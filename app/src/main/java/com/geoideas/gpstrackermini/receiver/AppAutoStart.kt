package com.geoideas.gpstrackermini.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.geoideas.gpstrackermini.service.WhereProcessor
import com.geoideas.gpstrackermini.util.AppConstant

class AppAutoStart : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val ls = prefs.getBoolean("location_service", false)
        val ss = prefs.getBoolean("sms_service", false)
        val ga = prefs.getBoolean("geofence_alert", false)
        if(ls) startWhereService("location_service", context)
        if(ss) startWhereService("sms_service", context)
        if(ga) startWhereService(context)
    }

    private fun startWhereService(preference: String, context: Context) {
        if(!(WhereProcessor.ACCEPTING_SMS || WhereProcessor.TRACKING))
            Intent(context, WhereProcessor::class.java).also {
                it.putExtra(AppConstant.PREFERENCE_CHANGED, true)
                it.putExtra("preference", preference)
                ContextCompat.startForegroundService(context, it)
            }
    }

    private fun startWhereService(context: Context) {
        Intent(context, WhereProcessor::class.java).also {
            ContextCompat.startForegroundService(context, it)
        }
    }
}
