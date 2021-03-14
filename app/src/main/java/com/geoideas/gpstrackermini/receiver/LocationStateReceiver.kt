package com.geoideas.gpstrackermini.receiver

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.geoideas.gpstrackermini.activity.util.ActivityUtils
import com.geoideas.gpstrackermini.service.WhereProcessor
import com.geoideas.gpstrackermini.util.AppConstant

class LocationStateReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if(context != null) {
            val prefsEdit = PreferenceManager.getDefaultSharedPreferences(context).edit()
            ActivityUtils().run {
                val active = isLocationOn(context) && isHighAccuracyMode(context)
                prefsEdit.putBoolean("location_service", active)
                    .putBoolean("geofence_alert", active)
                    .commit()
                if(!active)
                    Intent(context, WhereProcessor::class.java).also {
                        it.putExtra(AppConstant.STOP_SERVICE, true)
                        ContextCompat.startForegroundService(context, it)
                    }
            }
        }
    }


}