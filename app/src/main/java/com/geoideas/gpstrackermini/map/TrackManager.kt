package com.geoideas.gpstrackermini.map

import android.app.Activity
import android.content.Context
import android.widget.ProgressBar
import com.geoideas.gpstrackermini.R
import com.geoideas.gpstrackermini.activity.util.ActivityUtils
import com.geoideas.gpstrackermini.map.export.KMLExporterImpl
import com.google.android.gms.maps.GoogleMap
import com.geoideas.gpstrackermini.repository.room.entity.Point
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.maps.android.PolyUtil
import java.io.File
import kotlin.concurrent.thread

open class TrackManager(map: GoogleMap, context: Context): MapManager(map, context) {

    private val utils = ActivityUtils()
    private val exporter = KMLExporterImpl()
    private val lineSegments = mutableListOf<Polyline>()
    private var lineSegmentsInfo = mutableListOf<MarkerOptions>()
    private lateinit var infoMarker: Marker
    private lateinit var titleMarker: Marker
    private lateinit var line: Polyline
    private val speeds: MutableList<Float> = mutableListOf()
    private val durations: MutableList<Long> = mutableListOf()
    private val allSpeeds: MutableList<Float> = mutableListOf()
    private val allDurations: MutableList<Long> = mutableListOf()
    private val colours: MutableList<String> = mutableListOf()
    private val startTimes: MutableList<String> = mutableListOf()
    private val endTimes: MutableList<String> = mutableListOf()
    private val icon = bitmapDescriptorFromVector(R.drawable.ic_marker_icon)
    private val endMarkerIcon = bitmapDescriptorFromVector(R.drawable.ic_marker_end)
    private val startMarkerIcon = bitmapDescriptorFromVector(R.drawable.ic_marker_start)
    private lateinit var startMarker: Marker
    private lateinit var endMarker: Marker
    private var distance = 0.0f

    private fun addTitle(track: Track) {
        val ops = track.getTitleMarker()
        ops.icon(icon)
        titleMarker = map.addMarker(ops)
    }


    open fun showSegmentInfo(line: Polyline) {
        removeInfoMarker()
        val options = lineSegmentsInfo[lineSegments.indexOf(line)]
        infoMarker = map.addMarker(options)
        infoMarker.showInfoWindow()
    }

    open fun showTitle() = titleMarker.showInfoWindow()

    open fun createTrack(points: List<Point>, title: String, zoom: Float = map.cameraPosition.zoom) {
        if(points.isEmpty()) return
        val track = initTrack(points, title)
        addTitle(track)
        line = map.addPolyline(track.getTrack())
        distance = track.distances().sum()
        allSpeeds.addAll(track.speeds())
        allDurations.addAll(track.durations())
        addStartAndEndMarkers(points[0].moment, points.last().moment,line)
        this.moveCamera(track.getTrack().points[0], 14f)
        startTimes.add(points.first().moment)
        endTimes.add(points.last().moment)
    }

    open fun createGradientTrack(
        points: List<Point>,
        visiblePoints: List<Point>,
        title: String,
        max: Int,
        filterLevel: Int = 0,
        activity: Activity,
        bar: ProgressBar,
        onComplete: () -> Unit
    ) {
        if(points.isEmpty()) return;
        var moved = false
        val range = 5000
        val size = points.size
        var endIndex = 0
        Thread {
            for (startIndex in points.indices) {
                activity.runOnUiThread { utils.startStatusBar(bar) }
                if (startIndex < endIndex) continue
                endIndex = startIndex + range
                endIndex = if (endIndex < size) endIndex else size - 1
                val subPoints = points.subList(startIndex, endIndex)
                if (subPoints.isEmpty()) continue
                var track = initTrack(subPoints, title)
                var gradient = track.getGradientTrack(max)
                // keep track of all speeds regardless of filter
                allSpeeds.addAll(track.speeds())
                // keep track of all durations regardless of filter
                allDurations.addAll(track.durations())
                // distance calculate before filtering
                distance += track.distances().sum()
                if(filterLevel > 0) {
                    val clusterManager = TrackCluster(track, 15F * filterLevel)
                    val clusterPoints = clusterManager.cluster()
                    if (clusterPoints.isEmpty()) continue
                    track = initTrack(clusterPoints, title)
                    gradient = track.getGradientTrack(max)
                }
                speeds.addAll(track.speeds())
                durations.addAll(track.durations())
                colours.addAll(track.colours())
                startTimes.addAll(track.startTimes())
                endTimes.addAll(track.endTimes())
                lineSegmentsInfo.addAll(gradient.tracksInfo.toMutableList())
                activity.runOnUiThread {
                    lineSegments.addAll(gradient.tracks.map {
                        if(!moved) {
                            moved = true
                            this.moveCamera(it.points[0], 14f)
                        }
                        map.addPolyline(it)
                    })
                    utils.stopStatusBar(bar)
                }
            }
            activity.runOnUiThread {
                if(lineSegments.isNotEmpty()) {
                    addStartAndEndMarkers(
                        points[0].moment, points.last().moment,
                        lineSegments.first(), lineSegments.last())
                }
                utils.stopStatusBar(bar)
                onComplete()
            }
        }.start()
    }


/*    open fun createGradientTrack(points: List<Point>, visiblePoints: List<Point>, title: String, max: Int, activity: Activity) {
        if(points.isEmpty()) return;
        val range = 5000L
        val size = points.size
        points.stream()
            .limit((points.size - 1).toLong())
            .skip(range)
            .mapToInt(points::indexOf)
            .parallel
            .forEach { startIndex ->
                Log.d("TrackX", "--------------------------------")
                var start = System.currentTimeMillis()
                var endIndex = (startIndex + range).toInt()
                endIndex = if (endIndex < size) endIndex else size - 1
                val subPoints = points.subList(startIndex, endIndex)
                if (!subPoints.isEmpty()) {
                    val track = initTrack(subPoints, title)
                    track.getGradientTrack(max)
                    speeds.addAll(track.speeds())
                    durations.addAll(track.durations())
                    colours.addAll(track.colours())
                    var end = System.currentTimeMillis() - start
                    Log.d("TrackX", "all gradient $end")
                    val clusterManager = TrackCluster(track)
                    val clusterPoints = clusterManager.cluster()
                    start = System.currentTimeMillis()
                    if (!clusterPoints.isEmpty()) {
                        val clusterTrack = initTrack(clusterPoints, title)
                        val gradient = clusterTrack.getGradientTrack(max)
                        end = System.currentTimeMillis() - start
                        Log.d("TrackX", "cluster gradient ${System.currentTimeMillis() - start}")
                        val i = System.currentTimeMillis()
                        lineSegmentsInfo.addAll(gradient.tracksInfo.toMutableList())
                        activity.runOnUiThread {
                            val l = System.currentTimeMillis()
                            lineSegments.addAll(gradient.tracks.map { map.addPolyline(it) })
                            Log.d("TrackX", "map ${System.currentTimeMillis() - l}")
                        }
                    }
                }
            }
    }*/

    open fun clearTracks() {
        if(::titleMarker.isInitialized && ::line.isInitialized) {
            titleMarker.remove()
            line.remove()
            removeInfoMarker()
        }
        allSpeeds.clear()
        allDurations.clear()
        speeds.clear()
        durations.clear()
        colours.clear()
        startTimes.clear()
        endTimes.clear()
        lineSegmentsInfo.clear()
        lineSegments.forEach { it.remove() }
        lineSegments.clear()
        removeDistance()
        removeInfoMarker()
        removeStartAndEndMarkers()
    }

    fun withinBounds(points: List<Point>, polygon: List<LatLng>) = points.filter {
        PolyUtil.containsLocation(it.latitude, it.longitude, polygon, false)
    }

    fun exportTrack(handler: (File, String) -> Unit) {
        val points = line.points
        thread {
            exporter.exportTrack(points, startTimes.first(), endTimes.first())
            exporter.save(handler)
        }
    }

    fun exportGradientTrack(handler: (File, String) -> Unit) {
        val points = lineSegments.map { it.points }
        thread {
            exporter.exportTrackGradient(points, speeds, durations, colours,startTimes, endTimes)
            exporter.save(handler)
        }
    }

    private fun initTrack(points: List<Point>, title: String): Track {
        val middlePoint = points[points.size/2]
        return Track(points, title, LatLng(middlePoint.latitude, middlePoint.longitude))
    }

    private fun addStartAndEndMarkers(startTime: String, endTime: String, startLine: Polyline, endLine: Polyline? = null) {
        if(startLine.points == null || startLine.points.isEmpty()) return
        val start = startLine.points.first()
        startMarker = MarkerOptions().run {
            position(LatLng(start.latitude, start.longitude))
            title("Start - $startTime")
            icon(startMarkerIcon)
            map.addMarker(this)
        }
        val endPoints = endLine?.points ?: startLine.points
        if(endPoints.isNullOrEmpty()) return
        val end = if(endLine == null) startLine.points.last()
                    else if(endLine.points.isNotEmpty()) endLine.points.first()
                    else return
        endMarker = MarkerOptions().run {
            position(LatLng(end.latitude, end.longitude))
            title("End - $endTime")
            icon(endMarkerIcon)
            map.addMarker(this)
        }
    }

    private fun removeStartAndEndMarkers() {
        if(::startMarker.isInitialized) startMarker.remove()
        if(::endMarker.isInitialized) endMarker.remove()
    }

    private fun removeDistance() {
        distance = 0.0f
    }

    fun speeds() = speeds
    fun durations() = durations
    fun allDurations() = allDurations
    fun allSpeeds() = allSpeeds
    fun distance() = distance
    fun colours() = colours

    private fun removeInfoMarker() {
        if(::infoMarker.isInitialized) infoMarker?.remove()
    }

}