package com.geoideas.gpstrackermini.map

import android.app.Activity
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
    private var executor = Executors.newSingleThreadScheduledExecutor()
    private val repository = Repository(activity)

    var onUpdate: (Int, Int, Int) -> Unit = { _, _, _ -> Unit }

    private val pref = PreferenceManager.getDefaultSharedPreferences(activity)

    fun restart() {
        terminate()
        executor = Executors.newSingleThreadScheduledExecutor()
        start()
    }

    fun start() {
        running = true
        executor.scheduleAtFixedRate(::updateTrack, 0, 10, TimeUnit.SECONDS)
    }

    fun terminate() {
        map.clear()
        if(running) {
            running = false
            executor.shutdownNow()
        }
    }

    private fun updateTrack() {
        if(!running) return
        val points = repository.db.pointDao().fetchLastN(size)
        val track = Track(points)
        val path = track.getGradientTrack(speed, false)
        activity.runOnUiThread {
            speed = pref.getString("live_track_max", "$speed")?.toInt() ?: speed
            size = pref.getString("live_track_size", "$size")?.toInt() ?: size
            if(!(speed < speed || size < size) && path.tracks.isNotEmpty()) {
                val currentSpeed = track.speeds().last().toInt()
                val avgSpeed = track.speeds().average().toInt()
                val maxSpeed = track.speeds().max()?.toInt() ?: 0
                onUpdate(currentSpeed, avgSpeed, maxSpeed)
                map.clear()
                path.tracks.forEach {
                    map.addPolyline(it)
                }
            }
        }
    }

}