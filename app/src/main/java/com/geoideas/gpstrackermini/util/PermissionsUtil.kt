package com.geoideas.gpstrackermini.util

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.geoideas.gpstrackermini.activity.TrackActivity

object PermissionsUtil {

    val LOCATION_PERMISSION = "LOCATION_PERMISSION"
    val SMS_PERMISSION = "SMS_PERMISSION"
    val FILE_PERMISSION = "FILE_PERMISSION"
    val GET_PERMISSION = "GET_PERMISSION"
    val MISSING_PERMISSION = "MISSING_PERMISSION"

    fun resolveLocationPermission(context: Context) : Boolean {
        val has = ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if(!has) resolvePermissions(context, LOCATION_PERMISSION)
        return has
    }

    fun resolveFilePermission(context: Context) : Boolean {
        val has = ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
        if(!has) resolvePermissions(context, FILE_PERMISSION)
        return has
    }

    fun resolveSMSPermission(context: Context) : Boolean {
        val has = ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED
        if(!has) resolvePermissions(context, SMS_PERMISSION)
        return has
    }

    fun hasLocationPermission(context: Context) =
        ActivityCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    fun hasFilePermission(context: Context) =
        ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

    fun hasSMSPermission(context: Context) =
        ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED

    fun resolvePermissions(context: Context, perm: String) {
        Intent(context, TrackActivity::class.java).run {
            putExtra(MISSING_PERMISSION, true)
            putExtra(GET_PERMISSION, perm)
            context.startActivity(this)
        }
    }

}