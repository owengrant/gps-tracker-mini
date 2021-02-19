package com.geoideas.gpstrackermini.map

import android.location.Location
import com.geoideas.gpstrackermini.repository.room.entity.Point
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import java.text.SimpleDateFormat

object MapUtils {

    fun speed(t1: String, l1: LatLng, t2: String, l2: LatLng): Float {
        val secs = diffSeconds(t1, t2)
        return if(secs > 0) distance(l1, l2)/secs else 0f
    }


    fun diffSeconds(t1: String, t2: String): Long {
        val formatter = SimpleDateFormat("yy-MM-dd HH:mm:ss")
        val time1 = formatter.parse(t1)
        val time2 = formatter.parse(t2)
        return Math.abs((time1.time-time2.time)/1000)
    }

    fun distance(l1: LatLng, l2: LatLng): Float {
        val loc1 = Location("").apply {
            latitude = l1.latitude
            longitude = l1.longitude
        }
        val loc2 = Location("").apply {
            latitude = l2.latitude
            longitude = l2.longitude
        }
        return loc1.distanceTo(loc2)
    }

    fun distanceNative(p1: Point, p2: Point): Float {
        val lat_a = p1.latitude
        val lng_a = p1.longitude
        val lat_b = p2.latitude
        val lng_b = p2.longitude
        val earthRadius = 3958.75
        val latDiff = Math.toRadians((lat_b - lat_a).toDouble())
        val lngDiff = Math.toRadians((lng_b - lng_a).toDouble())
        val a =
            Math.sin(latDiff / 2) * Math.sin(latDiff / 2) + Math.cos(Math.toRadians(lat_a.toDouble())) * Math.cos(
                Math.toRadians(lat_b.toDouble())
            ) *
                    Math.sin(lngDiff / 2) * Math.sin(lngDiff / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        val distance = earthRadius * c

        val meterConversion = 1609

        return (distance * meterConversion).toFloat()
    }

    fun mapBounds(map: GoogleMap): List<LatLng> {
        val pj = map.projection.visibleRegion
        return listOf(pj.farLeft, pj.farRight, pj.nearRight, pj.nearLeft, pj.farLeft)
    }

}