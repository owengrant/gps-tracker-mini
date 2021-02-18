package com.geoideas.gpstracker.location

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.location.Location
import android.util.Log
import com.geoideas.gpstracker.repository.room.entity.Fence
import com.geoideas.gpstracker.service.WhereProcessor
import com.geoideas.gpstracker.util.AppConstant
import com.geoideas.gpstracker.util.PermissionsUtil
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task

class Locator(private val context: Context) {

    private val TAG = "Locator"

    val lP = LocationServices.getFusedLocationProviderClient(context)
    val gF = LocationServices.getGeofencingClient(context)
    private lateinit var trackingLCB: LocationCallback

    fun currentLocation(callBack: (loc: Location) -> Unit){
        if(!PermissionsUtil.hasLocationPermission(context)) {
            Log.d(TAG, "missing permission")
            return
        }
        val lcb = defaultLCB(callBack, true)
        Tracker.listen(context, lP, lcb)
    }

    fun startTracking(callBack: (loc: Location) -> Unit){
        if(!PermissionsUtil.hasLocationPermission(context)) {
            Log.d(TAG, "missing permission")
            return
        }
        trackingLCB = defaultLCB(callBack, false)
        Tracker.listen(context, lP, trackingLCB)
    }

    fun stopTracking() {
        if(::trackingLCB.isInitialized) lP.removeLocationUpdates(trackingLCB)
    }

    fun defaultLCB(callBack: (loc: Location) -> Unit, oneShot: Boolean): LocationCallback {
        lateinit var lcb: LocationCallback
        lcb = object: LocationCallback(){
            override fun onLocationResult(res: LocationResult?) {
                //do something if location is null
                res ?: return
                callBack(res.lastLocation)
                if(oneShot) lP.removeLocationUpdates(lcb)
            }
        }
        return lcb
    }

    fun addGeofence(f: Fence): Task<Void> {
        PermissionsUtil.resolveLocationPermission(context)
        val e1 = if(f.isEnter) Geofence.GEOFENCE_TRANSITION_ENTER else -1
        val e2 = if(f.isExit) Geofence.GEOFENCE_TRANSITION_EXIT else -1
        val fence = Geofence.Builder().run{
            setRequestId(f.key)
            setCircularRegion(f.latitude, f.longitude, f.radius)
            if(e1 > -1 && e2 > -1) setTransitionTypes(e1 or e2)
            else if(e1 > -1) setTransitionTypes(e1)
            else if(e2 > -1) setTransitionTypes(e2)
            setExpirationDuration(Geofence.NEVER_EXPIRE)
            setLoiteringDelay(3000)
            build()
        }
        val fenceRequest = GeofencingRequest.Builder().run {
            addGeofence(fence)
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            build()
        }
        val gIntent = Intent(context, WhereProcessor::class.java)
        gIntent.putExtra(AppConstant.GEOFENCE_EVENT, true)
        val pIntent = PendingIntent.getService(context,AppConstant.GEOFENCE_CODE, gIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        return gF.addGeofences(fenceRequest, pIntent)
    }

    fun removeGeoFence(key: String) = gF.removeGeofences(listOf(key))

}
