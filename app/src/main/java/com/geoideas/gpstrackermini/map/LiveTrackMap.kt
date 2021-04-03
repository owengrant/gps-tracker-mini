package com.geoideas.gpstrackermini.map

import android.app.Activity
import android.util.Log
import androidx.preference.Preference
import androidx.preference.PreferenceManager
import com.geoideas.gpstrackermini.repository.Repository
import com.geoideas.gpstrackermini.util.AppConstant
import com.google.android.gms.maps.GoogleMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class LiveTrackMap(val map: GoogleMap, val activity: Activity) {
    private var SPEED = AppConstant.LIVE_TRACK_SPEED_MIN
    private var SIZE = AppConstant.LIVE_TRACK_MAX_SIZE

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
        val points = repository.db.pointDao().fetchLastN(SIZE)
        val path = Track(points).getGradientTrack(SPEED)
        Log.d("LiveTrackMap", path.tracks.size.toString())
        activity.runOnUiThread {
            val max = pref.getString("live_track_max", "$SPEED")?.toInt() ?: SPEED
            val size = pref.getString("live_track_size", "$SIZE")?.toInt() ?: SIZE
            if(!(max < SPEED || size < SIZE)) {
                map.clear()
                path.tracks.forEach {
                    map.addPolyline(it)
                }
            }
        }
    }

}