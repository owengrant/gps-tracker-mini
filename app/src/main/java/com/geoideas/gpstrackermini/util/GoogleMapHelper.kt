package com.geoideas.gpstrackermini.util

import android.location.Location
import com.geoideas.gpstrackermini.repository.room.entity.Fence

object GoogleMapHelper {

    fun locationURL(location: Location): String = locationURL(location.latitude, location.longitude)

    fun locationURLText(location: Location): String = locationURLText(location.latitude, location.longitude)

    fun locationURL(lat: Double, long: Double) = "http://gpstrackerapp.com/#/gmap/$long/$lat/10"

    fun locationURLText(lat: Double, long: Double) = """
                    ${AppConstant.APP_NAME}-I am here- 
                    ${locationURL(lat, long)}
                """.trimIndent()

    fun fenceURL(fence: Fence) =
        "http://gpstrackerapp.com/#/gcircle/${round(fence.longitude)}/${round(fence.latitude)}/${fence.radius}/13"


    private fun round(value: Double) = String.format("%.5f", value);
}