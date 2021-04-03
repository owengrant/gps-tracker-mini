package com.geoideas.gpstrackermini.activity.listeners

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.geoideas.gpstrackermini.service.WhereProcessor
import com.geoideas.gpstrackermini.util.AppConstant

class SharedPreferenceListener(
    private val cxt: Context
) : SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onSharedPreferenceChanged(p0: SharedPreferences?, p1: String?) {
        if(p1 == "live_track_max" && p0 != null) {
            val max = p0.getString(p1, "100")?.toInt() ?: 100
            if(max < AppConstant.LIVE_TRACK_SPEED_MIN) {
                Toast.makeText(cxt, "Speed limit must not be less than ${AppConstant.LIVE_TRACK_SPEED_MIN}", Toast.LENGTH_LONG).show()
                p0.edit().putString(p1, "100").apply()
            }
        } else if(p1 == "live_track_size" && p0 != null) {
            var max = AppConstant.LIVE_TRACK_MAX_SIZE
            var min = AppConstant.LIVE_TRACK_MIN_SIZE
            val size = p0.getString(p1, "$max")?.toInt() ?: max
            if(size > max || size < min) {
                Toast.makeText(cxt, "Size must be within $min-$max", Toast.LENGTH_LONG).show()
                p0.edit().putString(p1, max.toString()).apply()
            }
        }
        else if(p1 == "live_accuracy" && p0 != null) {
            val default = 15
            val accuracy = p0.getString(p1, "$default")?.toInt() ?: 100
            if(accuracy < AppConstant.LIVE_TRACK_ACCURACY_MIN) {
                Toast.makeText(cxt, "GPS accuracy must not be less than ${AppConstant.LIVE_TRACK_ACCURACY_MIN}", Toast.LENGTH_LONG).show()
                p0.edit().putString(p1, "$default").apply()
            }
        }
        else if(p1 != "first")
            Intent(cxt, WhereProcessor::class.java).also {
                it.putExtra(AppConstant.PREFERENCE_CHANGED, true)
                it.putExtra("preference", p1)
                ContextCompat.startForegroundService(cxt, it)
            }
    }
}