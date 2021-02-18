package com.geoideas.gpstracker.repository

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.preference.PreferenceManager
import android.telephony.SmsManager
import android.util.Log
import com.geoideas.gpstracker.activity.CreateGeofenceActivity
import com.geoideas.gpstracker.R
import com.geoideas.gpstracker.notification.NotificationPublisher
import com.geoideas.gpstracker.repository.room.Database
import com.geoideas.gpstracker.repository.room.entity.Fence
import com.geoideas.gpstracker.repository.room.entity.FenceEvent
import com.geoideas.gpstracker.repository.room.entity.User
import com.geoideas.gpstracker.util.GoogleMapHelper
import com.geoideas.gpstracker.util.PermissionsUtil
import com.google.android.gms.location.Geofence
import java.text.SimpleDateFormat
import java.util.*


class Repository(val context: Context) {

    private val TAG = Repository::class.java.simpleName
    private val prefs = PreferenceManager.getDefaultSharedPreferences(context)

    val MAX_FENCING = 1
    val FENCE_CREATED = 0
    val FENCE_EVENT_NOTICE_ID = 7777

    val db = Database.getInstance(context)
    private val pointDao = db.pointDao()
    private val fenceDao = db.fenceDao()
    private val fenceEventDao = db.fenceEventDao()
    private val fenceUserAuthDao = db.fenceUserAuthDao()

    fun createFence(f: Fence): Int{
        return if(fenceDao.count() == 100) MAX_FENCING  else {
            fenceDao.insert(f)
            FENCE_CREATED
        }
    }

    fun recordFenceEvent(fE: FenceEvent) {
        Log.d(TAG, prefs.getBoolean("geofence_alert", false).toString())
        if(!prefs.getBoolean("geofence_alert", false)) return
        val fences = fenceDao.fetchByKey(fE.key)
        val fence = if(!fences.isEmpty()) fences[0] else return
        if(!fence.isActive) return
        val calendar = Calendar.getInstance()
        val date = calendar.getTime()
        val day = SimpleDateFormat("EEEE", Locale.ENGLISH).format(date.getTime())
        val today = fence.days.contains(day.toLowerCase())
        val between = timeBetween(fE.moment, fence.from, fence.to)
        if(today && between) {
            val event = if(Geofence.GEOFENCE_TRANSITION_ENTER == fE.transition) "enter" else "exit"
            if((fence.isEnter && event == "enter") || (fence.isExit && event == "exit")) {
                fenceEventDao.insert(fE)
                val users = fenceUserAuthDao.users(fence.id)
                notifyUsers(users, fence, event, fE.moment, fE)
            }
        }
    }

    open fun smsSend(address: String, message: String) =
        SmsManager.getDefault()
            .sendTextMessage(address, null, message, null, null)


    private fun timeBetween(time: String, from: String, to: String): Boolean {
        val fromTime = SimpleDateFormat("HH:mm:ss").parse(from).time
        val toTime = SimpleDateFormat("HH:mm:ss").parse(to).time
        val eventTime = SimpleDateFormat("HH:mm:ss").parse(time.split(" ")[1]).time
        return eventTime >= fromTime && eventTime <= toTime
    }

    private fun notifyUsers(users: List<User>, fence: Fence, event: String, moment: String, fenceEvent: FenceEvent) {
        val appName = context.getString(R.string.app_name)
        val allowed = if(fence.isSafe) "allowed" else "not allowed"
        val title = "$appName - ${event.capitalize()}ed ${fence.title}"
        val body = "${fence.description}. This area is $allowed"
        if(fence.isNotify)
            locationGeofenceEventNotice(fence, title, body)
        if(!PermissionsUtil.hasSMSPermission(context)) return
        val message = "$appName - ${event}ed ${fence.title} at $moment. This action is $allowed " +
                " ${GoogleMapHelper.fenceURL(fence)}"
        users.filter { fence.isSms }
            .filter { it.phoneNumber.isNotEmpty() }
            .forEach { smsSend(it.phoneNumber, message) }
    }

    private fun locationGeofenceEventNotice(fence: Fence, title: String, message: String) {
        val intent = Intent(context, CreateGeofenceActivity::class.java).apply {
            putExtra("geofence", fence)
            putExtra("update", true)
        }
        val noticeId = Random().nextInt(10000);
        val pIntent = PendingIntent.getActivity(
            context,
            noticeId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        NotificationPublisher.publish(pIntent, title, message, noticeId)
    }

}