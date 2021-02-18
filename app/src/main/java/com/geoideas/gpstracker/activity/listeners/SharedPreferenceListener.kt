package com.geoideas.gpstracker.activity.listeners

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.core.content.ContextCompat
import com.geoideas.gpstracker.service.WhereProcessor
import com.geoideas.gpstracker.util.AppConstant

class SharedPreferenceListener(
    private val cxt: Context
) : SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onSharedPreferenceChanged(p0: SharedPreferences?, p1: String?) {
        if(p1 != "first")
            Intent(cxt, WhereProcessor::class.java).also {
                it.putExtra(AppConstant.PREFERENCE_CHANGED, true)
                it.putExtra("preference", p1)
                ContextCompat.startForegroundService(cxt, it)
            }
    }
}