package com.geoideas.gpstracker.map

import android.content.Context
import com.geoideas.gpstracker.R
import com.geoideas.gpstracker.util.AppConstant.TITLE_MARKER
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions


class LocationManager(map: GoogleMap, context: Context): MapManager(map, context) {

    private val titles: MutableList<Point> = mutableListOf()
    private val points: MutableMap<String, Point> = mutableMapOf()
    private val lines: MutableMap<String, LineString> = mutableMapOf()

    fun showInfo() {
        points.values.forEach {
            it?.marker?.hideInfoWindow()
            val options = MarkerOptions().apply {
                val marker = it?.marker
                position(marker?.position as LatLng)
                icon(bitMapTitle(marker.title))
            }
            val titleMarker = map.addMarker(options).apply { tag = TITLE_MARKER }
            titles.add(Point(titleMarker, options))
        }
        lines.values.forEach {
            it?.midPoint?.hideInfoWindow()
            val options = MarkerOptions().apply {
                val marker = it?.midPoint
                position(marker?.position as LatLng)
                icon(bitMapTitle(marker.title))
            }
            val titleMarker = map.addMarker(options).apply { tag = TITLE_MARKER }
            titles.add(Point(titleMarker, options))
        }
    }

    fun hideInfo() {
        points.values.forEach { it?.marker?.hideInfoWindow() }
        titles.forEach { it?.marker?.remove() }
        titles.clear()
    }

    fun addPoint(point: Point, move: Boolean = true): Marker {
        //validate point
        val marker = map.addMarker(point.options.icon(bitmapDescriptorFromVector(R.drawable.ic_marker_default_icon)))
        point.marker = marker
        points[marker.id] = point
        if(move) moveCamera(marker.position, map.cameraPosition.zoom)
        return marker
    }

    fun removePoint(id: String): Boolean {
        return if(points.containsKey(id)) {
            val point = points[id]
            //remove from data model
            points.remove(id)
            //remove from map
            point?.marker?.remove()
            true
        } else false
    }

    fun addLine(line: LineString, title: String = "line"): Boolean {
        //validate point
        val polyline = map.addPolyline(line.options)
        val marker = MarkerOptions().let {
            it.title(title)
            it.position(linePoint(polyline.points))
            it.flat(true)
            it.icon(bitmapDescriptorFromVector(R.drawable.ic_marker_icon))
            map.addMarker(it)
        }
        line.line = polyline
        line.midPoint = marker
        lines[polyline.id] = line
        moveCamera(marker.position, map.cameraPosition.zoom)
        return true
    }

    fun removeLine(id: String): Boolean {
        return if(lines.containsKey(id)) {
            val line = lines[id]
            //remove from data model
            lines.remove(id)
            //remove from map
            line?.line?.remove()
            line?.midPoint?.remove()
            true
        } else false
    }

    fun findLine(id: String) = lines.get(id)

    fun findPoint(id: String) = points.get(id) ?: Point(options = MarkerOptions())

    fun clear() {
        map.clear()
        points.clear()
        lines.clear()
        titles.clear()
    }

}