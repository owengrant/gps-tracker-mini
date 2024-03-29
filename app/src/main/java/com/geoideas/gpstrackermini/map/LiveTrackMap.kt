package com.geoideas.gpstrackermini.map

import android.app.Activity
import android.util.Log
import androidx.preference.PreferenceManager
import com.geoideas.gpstrackermini.repository.Repository
import com.geoideas.gpstrackermini.util.AppConstant
import com.google.android.gms.maps.GoogleMap
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class LiveTrackMap(val map: GoogleMap, val activity: Activity) {
    private var speed = AppConstant.LIVE_TRACK_SPEED_MIN
    private var size = AppConstant.LIVE_TRACK_MAX_SIZE
    private var accuracy = AppConstant.LIVE_TRACK_ACCURACY_MIN

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
        executor.scheduleAtFixedRate(::updateTrack, 0, 6, TimeUnit.SECONDS)
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
        val lastPointList = repository.db.pointDao().fetchLast()

        // if the last recorded point is older than 5 minutes, do nothing
        if(lastPointList.isNotEmpty()) {
            val lastPoint = lastPointList.first()
            val moment = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(lastPoint.moment)
            if(System.currentTimeMillis() - moment.time > (1000*60*5)) return
        }

        val points = repository.db.pointDao().fetchLastNAccuracy(size, accuracy.toDouble())
        if(points.isNotEmpty()) {
            val moment = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(points.last().moment)
            if(System.currentTimeMillis() - moment.time > (1000*60*10)) return
        }
        val track = Track(points)
        val path = track.getGradientTrack(speed, false)
        activity.runOnUiThread {
            speed = pref.getString("live_track_max", "$speed")?.toInt() ?: speed
            size = pref.getString("live_track_size", "$size")?.toInt() ?: size
            accuracy = pref.getString("live_accuracy", "$accuracy")?.toInt() ?: accuracy
            if(!(speed < speed || size < size) && path.tracks.isNotEmpty()) {
                val currentSpeed = track.speeds().first().toInt()
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