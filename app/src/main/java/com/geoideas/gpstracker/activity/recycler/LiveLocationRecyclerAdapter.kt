package com.geoideas.gpstracker.activity.recycler

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.geoideas.gpstracker.R
import com.geoideas.gpstracker.activity.recycler.viewholders.query.LiveLocationViewHolder
import com.geoideas.gpstracker.repository.room.entity.Device
import com.google.android.gms.maps.model.LatLng

class LiveLocationRecyclerAdapter (
    private val devices: List<Device>,
    context: Context,
    private val goto: (LatLng) -> Unit
) : RecyclerView.Adapter<LiveLocationViewHolder>() {

    private val inflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LiveLocationViewHolder {
        val view = inflater.inflate(R.layout.live_search_item, parent, false)
        return LiveLocationViewHolder(view, goto)
    }

    override fun getItemCount() = devices.size

    override fun onBindViewHolder(holder: LiveLocationViewHolder, position: Int)  =
        holder.load(devices[position])
}