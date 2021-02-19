package com.geoideas.gpstrackermini.activity.util

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.LocationManager
import android.location.LocationManager.GPS_PROVIDER
import android.location.LocationManager.NETWORK_PROVIDER
import android.provider.Settings
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.geoideas.gpstrackermini.R
import com.geoideas.gpstrackermini.repository.room.entity.User
import com.geoideas.gpstrackermini.util.AppConstant
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.material.snackbar.Snackbar


class ActivityUtils {

    fun viewToImage(view: View): Bitmap {
        val map = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(map)
        view.draw(canvas)
        return map
    }

    fun showSnackBar(view: View, mes: String, action: String = "", callback: (View) -> Unit = {}) {
        val sb = Snackbar.make(view, mes, Snackbar.LENGTH_LONG)
        if(action.isNotBlank()) {
            sb.setAction(action, callback)
        }
        sb.show()
    }

    fun validateAndShow(view: View, user: User): Boolean{
        if(user.name.isBlank()) {
            showSnackBar(view,"username cannot be empty")
            return false
        }
        if(user.name.length < 5) {
            showSnackBar(view,"username must be at least five characters")
            return false
        }
        if(user.code.isBlank()) {
            showSnackBar(view,"code cannot be empty")
            return false
        }
        if(user.code.length < 5) {
            showSnackBar(view,"code must be at least five characters")
            return false
        }
        return true
    }

    fun hasLocationPermission(context: Context) =  ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

    fun invalidEmail(email: EditText) = !android.util.Patterns.EMAIL_ADDRESS.matcher(email.text.toString()).matches()

    fun invalidPhoneNumber(email: EditText) = !android.util.Patterns.PHONE.matcher(email.text.toString()).matches()

    fun stopStatusBar(bar: ProgressBar) {
        bar.isIndeterminate = false
    }

    fun startStatusBar(bar: ProgressBar) {
        bar.isIndeterminate = true
    }

    fun showPurchaseDialog(ctx: Context, message: String) {
        AlertDialog.Builder(ctx, R.style.AlertDialogTheme).apply {
            setTitle("Download Geo SMS Pro")
            setMessage("$message\nPlease click upgrade to download Geo SMS Pro.")
            setNegativeButton("Okay") { d, _ -> d.dismiss() }
            setPositiveButton("Upgrade") { d, _ -> d.dismiss() }
        }.show()
    }

    fun onlyGPSMode(ctx: Context) : Boolean {
        val locationManager = ctx.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        return (locationManager != null &&
            (locationManager!!.isProviderEnabled(GPS_PROVIDER) &&
            !locationManager!!.isProviderEnabled(NETWORK_PROVIDER))
        )
    }

    fun showLocationModeChangeDialog(ctx: Context, message: String = "Please change your GPS mode to Battery Saving or High Accuracy.") {
        AlertDialog.Builder(ctx, R.style.AlertDialogTheme).apply {
            setTitle("Change GPS Mode")
            setMessage(message)
            setNegativeButton("Cancel") { d, _ -> d.dismiss() }
            setPositiveButton("Change") { _, _ -> ctx.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)); }
        }.show()
    }

    fun createLocationModeChangeDialog(
        ctx: Context,
        message: String = "Please change your GPS mode to Battery Saving or High Accuracy."
    ) =
        androidx.appcompat.app.AlertDialog.Builder(ctx, R.style.AlertDialogTheme).run {
            setTitle("Change GPS Mode")
            setMessage(message)
            setNegativeButton("Cancel") { d, _ -> d.dismiss() }
            setPositiveButton("Change") { _, _ ->
                ctx.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
            create()
        }

    fun isLocationOn(ctx: Context) : Boolean {
        val locationManager = ctx.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        return (locationManager != null &&
                (locationManager!!.isProviderEnabled(GPS_PROVIDER) &&
                        locationManager!!.isProviderEnabled(NETWORK_PROVIDER))
                )
    }

    fun initAds(ctx: Context) : InterstitialAd {
        val app: ApplicationInfo = ctx.packageManager
            .getApplicationInfo(ctx.packageName, PackageManager.GET_META_DATA)
        val adId = app.metaData.getString("com.google.android.gms.ads.APPLICATION_ID");
        val interstitialAd = InterstitialAd(ctx)
        interstitialAd.adUnitId = AppConstant.AD_UNIT_IT
        return interstitialAd;
    }

    fun loadAd(interstitialAd: InterstitialAd) {
        interstitialAd.loadAd(AdRequest.Builder().build())
    }

    fun showAd(interstitialAd: InterstitialAd) {
        if(interstitialAd.isLoaded) {
            interstitialAd.show()
        }
    }

}