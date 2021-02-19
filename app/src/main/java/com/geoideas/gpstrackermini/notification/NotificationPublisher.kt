package com.geoideas.gpstrackermini.notification

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationCompat
import com.geoideas.gpstrackermini.R

@SuppressLint("StaticFieldLeak")
object NotificationPublisher {

    private val TAG = NotificationPublisher::class.java.simpleName
    private var nChannelID = ""
    private lateinit var nManager: NotificationManager
    private lateinit var cxt: Context // while memory leak the context will be the background service

    fun create(context: Context, nManager: NotificationManager, nChannel: String) {
        this.cxt = context
        this.nManager = nManager
        this.nChannelID = nChannel
    }

    fun publish(intent: PendingIntent? = null, title: String, text: String,nid: Int) {
        val notice = NotificationCompat.Builder(cxt, nChannelID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_logo_notice)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
        if(intent != null) notice.setContentIntent(intent)
        val notification = notice.build()
        notification.flags = notification.flags or Notification.FLAG_INSISTENT
        nManager.notify(nid, notification)
    }

    fun context() = cxt
}