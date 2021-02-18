package com.geoideas.gpstracker.activity.recycler.viewholders.query

import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.geoideas.gpstracker.R
import com.geoideas.gpstracker.repository.room.entity.Device
import com.google.android.gms.maps.model.LatLng

class LiveLocationViewHolder (
    view: View,
    private val goto: (LatLng) -> Unit
) : RecyclerView.ViewHolder(view) {

    private var name = view.findViewById<TextView>(R.id.text_name)
    private var latest = view.findViewById<TextView>(R.id.text_latest)
    private val gotoBtn = view.findViewById<Button>(R.id.btn_goto)
    private lateinit var device: Device

    init {
        gotoBtn.setOnClickListener {
            goto(LatLng(device.latitude, device.longitude))
        }
    }

    fun load(device: Device) {
        this.device = device
        name.text = device.name
        latest.text = device.moment
    }

}