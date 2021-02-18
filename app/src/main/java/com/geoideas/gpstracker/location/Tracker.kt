package com.geoideas.gpstracker.location

import android.content.Context
import android.util.Log
import com.geoideas.gpstracker.util.PermissionsUtil
import com.google.android.gms.location.*

object Tracker {

    private final val TAG = "Tracker"

     fun listen(context: Context, lP: FusedLocationProviderClient, lcb: LocationCallback){
        PermissionsUtil.resolveLocationPermission(context)
        val request = LocationRequest.create().apply {
            interval = 3000
            fastestInterval = 3000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        val settings = LocationSettingsRequest.Builder()
            .addLocationRequest(request)
            .build()
        val settingsCheck = LocationServices.getSettingsClient(context)
            .checkLocationSettings(settings)
        settingsCheck.addOnSuccessListener { lP.requestLocationUpdates(request, lcb, null) }
        settingsCheck.addOnFailureListener { Log.d(TAG, it.localizedMessage) }
    }

}