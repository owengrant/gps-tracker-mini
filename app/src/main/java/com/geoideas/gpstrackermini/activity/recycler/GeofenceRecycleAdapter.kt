package com.geoideas.gpstrackermini.activity.recycler

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.geoideas.gpstrackermini.R
import com.geoideas.gpstrackermini.activity.CreateGeofenceActivity
import com.geoideas.gpstrackermini.activity.GeofenceMapFragment
import com.geoideas.gpstrackermini.repository.Repository
import com.geoideas.gpstrackermini.repository.room.entity.Fence
import kotlin.concurrent.thread

class GeofenceRecycleAdapter (
        private val fences: List<Fence>,
        private val context: Context,
        private val repo: Repository
    ): RecyclerView.Adapter<GeofenceRecycleAdapter.GeofenceViewHolder>() {

    private val inflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int) = GeofenceViewHolder(inflater.inflate(R.layout.geofence_item, parent, false), this)

    override fun getItemCount() =  fences.size

    override fun onBindViewHolder(holder: GeofenceViewHolder, item: Int) = holder.display(fences[item])

    inner class GeofenceViewHolder(
        private val view: View,
        private val adapter: GeofenceRecycleAdapter
    ) : RecyclerView.ViewHolder(view), View.OnClickListener {

        private val label = view.findViewById<TextView>(R.id.txt_geofence_title)
        private val desc = view.findViewById<TextView>(R.id.txt_geofence_desc)
        private val viewBtn = view.findViewById<ImageView>(R.id.btn_view)
        private val activeSwitch = view.findViewById<Switch>(R.id.switch_active)

        override fun onClick(v: View?) {
            val intent = Intent(context, CreateGeofenceActivity::class.java)
            intent.putExtra("update", true)
            intent.putExtra("geofence", fences[layoutPosition])
            context.startActivity(intent)
        }

        fun display(fence: Fence) {
            label.text = fence.title
            desc.text = fence.description
            activeSwitch.isChecked = fence.isActive
            view.setOnClickListener(this)
            viewBtn.setOnClickListener { GeofenceMapFragment.viewOnMap(fence) }
            activeSwitch.setOnCheckedChangeListener { btn, checked ->
                thread {
                    fence.isActive = checked
                    repo.db.fenceDao().update(fence)
                }
            }
        }
    }
}