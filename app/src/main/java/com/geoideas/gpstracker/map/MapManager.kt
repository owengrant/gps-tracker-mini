package com.geoideas.gpstracker.map

import android.content.Context
import android.graphics.*
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng

open class MapManager(val map: GoogleMap, val context: Context) {

    fun linePoint(points: MutableList<LatLng>) = middle(points[points.indexOf(points.last())-1], points.last())

    fun middle(latlng1: LatLng, latlng2: LatLng): LatLng = LatLng((latlng1.latitude+latlng2.latitude)/2,(latlng1.longitude+latlng2.longitude)/2)

    fun moveCamera(latlng: LatLng, zoom: Float = 18f) = map.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, zoom))

    fun jumpCamera(latlng: LatLng, zoom: Float = map.cameraPosition.zoom) = map.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, zoom))

    open fun bitmapDescriptorFromVector(
        @DrawableRes vectorDrawableResourceId: Int,
        width: Int = -1,
        height: Int = -1 ):
    BitmapDescriptor {
        val background = ContextCompat.getDrawable(context, vectorDrawableResourceId)
        val w = if(width == -1) background!!.intrinsicWidth else width
        val h = if(height == -1) background!!.intrinsicWidth else height
        background!!.setBounds(0, 0, w, h)
        val vectorDrawable = ContextCompat.getDrawable(context, vectorDrawableResourceId)
        vectorDrawable!!.setBounds(40, 20, vectorDrawable.intrinsicWidth + 40, vectorDrawable.intrinsicHeight + 20)
        val bitmap = Bitmap.createBitmap(background.intrinsicWidth, background.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        background.draw(canvas)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    open fun bitMapTitle(title: String): BitmapDescriptor {
        val titleLength = if(title.length == 0) 1 else title.length*24
        val bitmap = Bitmap.createBitmap(titleLength, 60, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint().apply {
            textSize = 24f
            color = Color.BLACK
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawColor(Color.TRANSPARENT)
        canvas.drawText(title, 0f, 20f, paint)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

}