package com.geoideas.gpstrackermini.service

import android.annotation.SuppressLint
import android.app.*
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.preference.PreferenceManager
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.geoideas.gpstrackermini.R
import com.geoideas.gpstrackermini.activity.SettingsActivity
import com.geoideas.gpstrackermini.activity.util.ActivityUtils
import com.geoideas.gpstrackermini.coms.CommandExecutor
import com.geoideas.gpstrackermini.location.Locator
import com.geoideas.gpstrackermini.notification.NotificationPublisher
import com.geoideas.gpstrackermini.receiver.SMSReceiver
import com.geoideas.gpstrackermini.repository.Repository
import com.geoideas.gpstrackermini.repository.room.entity.Fence
import com.geoideas.gpstrackermini.repository.room.entity.FenceEvent
import com.geoideas.gpstrackermini.repository.room.entity.Point
import com.geoideas.gpstrackermini.util.AppConstant
import com.geoideas.gpstrackermini.util.PreferenceUtil
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import java.lang.Exception
import java.util.concurrent.Executors

class WhereProcessor : Service() {

    private  val TAG = "WhereProcessor"

    val SERVICE_NOTICE_ID = 1000
    val NOTICE_CHANNEL_ID = "com.geoideas.gpstracker.service"
    val MESSAGE_CHANNEL_ID = "com.geoideas.gpstracker.coms.websocket"
    private lateinit var notificationManager: NotificationManager
    private val timer = Executors.newScheduledThreadPool(1)
    private lateinit var repo: Repository
    private lateinit var commandExecutor: CommandExecutor
    private lateinit var locator: Locator
    private lateinit var prefs: SharedPreferences
    private lateinit var prefsUtil: PreferenceUtil
    private val activityUtils = ActivityUtils()

    companion object {
        var TRACKING = false
        var ACCEPTING_SMS = false
    }

    override fun onBind(intent: Intent): IBinder? { return null }

    override fun onCreate() {
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        prefs = PreferenceManager.getDefaultSharedPreferences(this)
        prefsUtil = PreferenceUtil(this)
        locator = Locator(this)
        repo = Repository(this)
        commandExecutor = CommandExecutor(this, locator, repo)
        createNotificationChannels()
        NotificationPublisher.create(this.applicationContext, notificationManager,NOTICE_CHANNEL_ID)
        initServiceNotice()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if(intent?.extras?.isEmpty == false) {
            val extras = intent?.extras
            if (extras.containsKey(AppConstant.PREFERENCE_CHANGED))
                handlePreferenceChange(extras)
            else if (extras.containsKey(AppConstant.GEOFENCE_EVENT)) fence(intent)
        }
        return START_STICKY
    }

    @SuppressLint("NewApi")
    private fun initServiceNotice(){
        //val useChannel = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
        //if(useChannel) notificationManager.createNotificationChannel(createNotificationChannel())
        startForeground(SERVICE_NOTICE_ID, createNotice())
    }

    @SuppressLint("NewApi")
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val appName = getString(R.string.app_name)
        val messageChannel = createNotificationChannel(
            NOTICE_CHANNEL_ID,
            NotificationCompat.PRIORITY_HIGH,
            appName,
            "$appName location and communication service"
        )
        messageChannel.enableVibration(true)
        messageChannel.description = appName
        notificationManager.createNotificationChannel(messageChannel)
    }

    @SuppressLint("NewApi")
    private fun createNotificationChannel(id: String, priority: Int, name: String, desc: String ) =
        NotificationChannel(id, name, priority).apply { description = desc }

    private fun createNotice(): Notification {
        val intent = Intent(this, SettingsActivity::class.java)
        val pIntent = PendingIntent.getActivity(
            this, SERVICE_NOTICE_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT
        )
        return NotificationCompat.Builder(this, NOTICE_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_logo_notice)
            .setStyle(NotificationCompat.BigTextStyle().bigText(createNotificationText()))
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(pIntent)
            .build()
    }

    private fun createNotificationText() : String {
        val trackingOn = prefs.getBoolean("location_service", false)
        val trackOnMessage = prefs.getString("tracking_on_message", "")
        val trackOffMessage = prefs.getString("tracking_off_message", "")
        val trackMessage = if(trackingOn) trackOnMessage else trackOffMessage
        val fenceOn = prefs.getBoolean("geofence_alert", false)
        val fenceOnMessage = prefs.getString("geofence_on_message", "")
        val fenceOffMessage = prefs.getString("geofence_off_message", "")
        val fenceMessage = if(fenceOn) fenceOnMessage else fenceOffMessage
        return "$trackMessage\n$fenceMessage"
    }


    private fun startTracker() {
        if(!activityUtils.isLocationOn(this)) {
            Toast.makeText(this, "Location tracking is off. Turn on device location and retry.", Toast.LENGTH_LONG)
                .show()
            prefs.edit().run {
                putBoolean("location_service", false)
                apply()
            }
        }
        else {
            Locator(this).startTracking {
                if (prefsUtil.hasLocationService()) {
                    Thread {
                        val dao = repo.db.pointDao()
                        val point = Point().fromLocation(it)
                        dao.insert(point)
                    }.start()
                }
            }
            TRACKING = true
        }
    }

    private fun stopTracking() {
        locator.stopTracking()
        TRACKING = false
    }

    private fun handlePreferenceChange(data: Bundle) {
        notificationManager.notify(SERVICE_NOTICE_ID, createNotice())
        val pref = data.getCharSequence("preference")
        when(pref) {
            "location_service" -> {
                if(prefsUtil.hasLocationService()) {
                    startTracker()
                }
                else {
                    stopTracking()
                    if(!prefsUtil.hasSMSService() && !prefsUtil.hasGeofenceAlert()) {
                        try {
                            stopSelf()
                        } catch (e: Exception) {
                            Log.e(TAG, e.localizedMessage)
                        }
                    }
                }
            }
            "geofence_alert" -> {
                if(
                    !prefsUtil.hasGeofenceAlert() &&
                    !prefsUtil.hasLocationService() &&
                    !prefsUtil.hasSMSService()
                )
                    try {
                        stopSelf()
                    } catch (e: Exception) {
                        Log.e(TAG, e.localizedMessage)
                    }
            }
        }
    }

    private fun fence(value: Intent?) {
        value ?: return
        if(!prefsUtil.hasGeofenceAlert()) {
            stopSelf()
            return
        }
        Thread {
            val fence = GeofencingEvent.fromIntent(value)
            if (!fence.hasError()) {
                fence.triggeringGeofences.map {
                    FenceEvent().apply {
                        transition = fence.geofenceTransition
                        key = it.requestId
                        val loc = fence.triggeringLocation
                        latitude = loc.latitude
                        longitude = loc.longitude
                        accuracy = if (loc.hasAccuracy()) loc.accuracy.toDouble() else 0.0
                    }
                }
                .filter(::filterFence)
                .forEach { repo.recordFenceEvent(it) }
            }
        }.start()
    }

    private fun filterFence(fE: FenceEvent): Boolean {
        val fences = repo.db.fenceDao().fetchByKey(fE.key)
        if(fences.isEmpty()) return false
        val fence = fences[0]
        if(!fence.isActive) return false
        return checkFenceTransition(fence, fE)
    }

    private fun checkFenceTransition(f: Fence, fE: FenceEvent): Boolean {
       return  if(f.isEnter && fE.transition == Geofence.GEOFENCE_TRANSITION_ENTER) true
            else if(f.isExit && fE.transition == Geofence.GEOFENCE_TRANSITION_EXIT) true
            else f.isDwell && fE.transition == Geofence.GEOFENCE_TRANSITION_DWELL
    }

}
