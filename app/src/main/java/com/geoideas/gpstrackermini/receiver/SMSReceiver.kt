package com.geoideas.gpstrackermini.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.preference.PreferenceManager
import android.telephony.SmsMessage
import com.geoideas.gpstrackermini.util.PermissionsUtil

class SMSReceiver(val handle: (sms: SmsMessage) -> Unit) : BroadcastReceiver() {

    private val PDU_TYPE = "pdus"

    override fun onReceive(context: Context, intent: Intent) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val active = prefs.getBoolean("sms_service", false)
        val permissions = PermissionsUtil.hasLocationPermission(context) &&
                PermissionsUtil.hasSMSPermission(context)
        if(!active || !permissions) return
        val bundle = intent.extras
        val format = bundle.getString("format")
        val pdus: Array<Any>? = bundle.get(PDU_TYPE) as Array<Any>
        pdus ?: return
        pdus.map { it as ByteArray }
            .forEach {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) handle(SmsMessage.createFromPdu(it, format))
            else handle(SmsMessage.createFromPdu(it))
        }

    }
}
