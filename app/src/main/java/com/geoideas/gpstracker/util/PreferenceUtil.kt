package com.geoideas.gpstracker.util

import android.content.Context
import android.preference.PreferenceManager

class PreferenceUtil(private val cxt: Context) {

    private val prefs = PreferenceManager.getDefaultSharedPreferences(cxt)

    fun hasLocationService() = prefs.getBoolean("location_service", false)

    fun hasSMSService() = prefs.getBoolean("sms_service", false)

    fun hasGeofenceAlert() = prefs.getBoolean("geofence_alert", false)

}