package com.geoideas.gpstrackermini.map

import android.app.Activity
import android.util.Log
import androidx.preference.PreferenceManager
import com.geoideas.gpstrackermini.repository.Repository
import com.geoideas.gpstrackermini.util.AppConstant
import com.google.android.gms.maps.GoogleMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class LiveTrackMap(val map: GoogleMap, val activity: Activity) {
    private var speed = AppConstant.LIVE_TRACK_SPEED_MIN
    private var size = AppConstant.LIVE_TRACK_MAX_SIZE

    private var running = false
    private val executor = Executors.newSingleThreadScheduledExecutor()
    private val repository = Repository(activity)

    private val pref = PreferenceManager.getDefaultSharedPreferences(activity)

    fun restart() {
        stop()
        start()
    }

    fun start() {
        executor.scheduleAtFixedRate(::updateTrack, 0, 10, TimeUnit.SECONDS)
    }

    fun stop() {
        map.clear()
        if(running) executor.shutdownNow()
    }

    private fun updateTrack() {
        running = true
        val points = repository.db.pointDao().fetchLastN(size)
        val path = Track(points).getGradientTrack(speed, false)
        activity.runOnUiThread {
            speed = pref.getString("live_track_max", "$speed")?.toInt() ?: speed
            size = pref.getString("live_track_size", "$size")?.toInt() ?: size
            if(!(speed < speed || size < size)) {
                map.clear()
                path.tracks.forEach {
                    map.addPolyline(it)
                }
            }
        }
    }

}