package com.geoideas.gpstrackermini.map

import android.graphics.Color
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.geoideas.gpstrackermini.repository.room.entity.Point
import com.geoideas.gpstrackermini.util.AppConstant.GREEN_RED_COLOURS
import com.google.android.gms.maps.model.LatLng

open class Track(val points: List<Point>, val title: String = "", val titlePosition: LatLng = LatLng(0.0, 0.0)) {

    private val tracks: MutableList<PolylineOptions> = mutableListOf()
    private val tracksInfo: MutableList<MarkerOptions> = mutableListOf()
    private val markers: MutableList<MarkerOptions> = mutableListOf()
    private lateinit var track: PolylineOptions
    private val speeds: MutableList<Float> = mutableListOf()
    private val durations: MutableList<Long> = mutableListOf()
    private val distances: MutableList<Float> = mutableListOf()
    private val colours: MutableList<String> = mutableListOf()
    private val startTimes: MutableList<String> = mutableListOf()
    private val endTimes: MutableList<String> = mutableListOf()


    private var titleMarker: MarkerOptions = MarkerOptions().also {
        it.title(title)
        it.position(titlePosition)
    }

    fun getTrack(): PolylineOptions {
        if(!::track.isInitialized) createTrack()
        return track
    }

    fun createTrack(): Track {
        if(::track.isInitialized) return this
        track = createPolylineOptions()
        val pIt = points.iterator()
        var p1: Point
        var p2: Point
        var last: Point? = null
        while(pIt.hasNext()) {
            p1 = last ?: pIt.next()
            if(pIt.hasNext()) {
                p2 = pIt.next()
                last = p2
                val l1 = LatLng(p1.latitude, p1.longitude)
                val l2 = LatLng(p2.latitude, p2.longitude)
                track.add(l1).add(l2)
                val duration = MapUtils.diffSeconds(p1.moment, p2.moment)
                durations.add(duration)
                val distance = MapUtils.distance(l1, l2)
                distances.add(distance)
                val speed = if(duration > 0) speedKMPH(distance, duration) else 0f
                speeds.add(speed)
            }
        }
        return this
    }


    private fun createGradient(max: Int): Track {
        markers.addAll(createMarkers())
        val pIt = points.iterator()
        var p1: Point
        var p2: Point
        var last: Point? = null
        while(pIt.hasNext()) {
            p1 = last ?: pIt.next()
            if(pIt.hasNext()) {
                p2 = pIt.next()
                last = p2
                val track = createPolylineOptions()
                val l1 = LatLng(p1.latitude, p1.longitude)
                val l2 = LatLng(p2.latitude, p2.longitude)
                startTimes.add(p1.moment)
                endTimes.add(p2.moment)
                val duration = MapUtils.diffSeconds(p1.moment, p2.moment)
                durations.add(duration)
                val distance = MapUtils.distance(l1, l2)
                distances.add(distance)
                val speed = if(duration > 0) speedKMPH(distance, duration) else 0f
                speeds.add(speed)
                track.add(l1)
                track.add(l2)
                val colour = trackSegmentColour(speed.toInt(), max)
                colours.add(colour)
                track.color(Color.parseColor(colour))
                tracksInfo.add(MarkerOptions().apply {
                    title("${speed.toInt()} kmph | ${p1.moment} | ${duration} sec")
                    position(middle(l1, l2))
                })
                tracks.add(track)
            }
        }
        return this
    }

    private fun speedKMPH(distance: Float, duration: Long) = ((distance/duration)*60*60)/1000f
/*
    private fun createGradientParallel(max: Int): Track {
        markers.addAll(createMarkers())
        points.stream()
            .limit((points.size -1).toLong())
            .mapToInt(points::indexOf)
            .parallel()
            .forEach {
                val p1 = points[it]
                val p2 = points[it+1]
                val track = createPolylineOptions()
                val l1 = LatLng(p1.latitude, p1.longitude)
                val l2 = LatLng(p2.latitude, p2.longitude)
                val speed = (MapUtils.speed(p1.moment, l1, p2.moment, l2)*3600)/1000
                speeds.add(speed.toInt())
                val duration = MapUtils.diffSeconds(p1.moment, p2.moment)
                durations.add(duration.toInt())
                track.add(l1)
                track.add(l2)
                val colour = trackSegmentColour(speed.toInt(), max)
                colours.add(colour)
                track.color(Color.parseColor(colour))
                tracksInfo.add(MarkerOptions().apply {
                    title("${speed.toInt()} km/h | ${p1.moment} | ${duration} sec")
                    position(middle(l1, l2))
                })
                tracks.add(track)
            }
        return this
    }
*/

    fun trackSegmentColour(value: Int, maximum: Int): String {
        val max = maximum - 1
        val per = (GREEN_RED_COLOURS.size/max).toFloat()
        val i = if(value >= max) GREEN_RED_COLOURS.size - 1 else (value*per).toInt()
        return GREEN_RED_COLOURS[i]
    }

    private fun createMarkers() = points.map {
        MarkerOptions().apply {
            title(it.moment)
            position(LatLng(it.latitude, it.longitude))
        }
    }

    private fun createPolylineOptions(colour: Int = Color.BLUE) =  PolylineOptions().apply {
        color(colour)
        width(5f)
        clickable(true)
    }


    fun middle(latlng1: LatLng, latlng2: LatLng): LatLng = LatLng((latlng1.latitude+latlng2.latitude)/2,(latlng1.longitude+latlng2.longitude)/2)


    fun speeds() = speeds

    fun durations() = durations

    fun distances() = distances

    fun colours() = colours

    fun startTimes() = startTimes

    fun endTimes() = endTimes

    fun getMarkers() = markers

    fun getTracks() = tracks

    fun getTitleMarker() = titleMarker

    fun getGradientTrack(max: Int): GradientTrack {
        createGradient(max)
        return GradientTrack(markers, tracks, tracksInfo)
    }

}