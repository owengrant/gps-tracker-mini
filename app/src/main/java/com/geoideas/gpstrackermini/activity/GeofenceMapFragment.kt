package com.geoideas.gpstrackermini.activity


import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.geoideas.gpstrackermini.R
import com.geoideas.gpstrackermini.util.PermissionsUtil
import com.geoideas.gpstrackermini.repository.Repository
import com.geoideas.gpstrackermini.repository.room.entity.Fence
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng

/**
 * A simple [Fragment] subclass.
 */
class GeofenceMapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var repo: Repository
    private val fenceZones = HashMap<String, Fence>()
    private var cameraPosition: CameraPosition? = null

    companion object {
        lateinit var mMap: GoogleMap

        fun viewOnMap(fence: Fence) {
            GeofenceTabActivity.viewPager.currentItem = 0
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(fence.latitude, fence.longitude), 18f))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_geofence_map, container, false)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        restoreState(savedInstanceState)
        return view
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if(this::mMap.isInitialized) {
            val center = mMap.cameraPosition.target
            val zoom = mMap.cameraPosition.zoom
            outState.putDouble("latitude", center.latitude)
            outState.putDouble("longitude", center.longitude)
            outState.putFloat("zoom", zoom)
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        GeofenceMapFragment.mMap = googleMap
        if(PermissionsUtil.hasLocationPermission(this.requireContext()))
            mMap.isMyLocationEnabled = true
        mMap.setOnCircleClickListener(::onFenceClicked)
        cameraPosition?.run { mMap.moveCamera(CameraUpdateFactory.newCameraPosition(this)) }
        loadData()
    }

    private fun restoreState(state: Bundle?) {
        state?.run {
            val latLng = LatLng(state.getDouble("latitute"), state.getDouble("longitude"))
            val zoom = state.getFloat("zoom")
            cameraPosition = CameraPosition(latLng, zoom, 0f, 0f)
        }
    }

    private fun onFenceClicked(circle: Circle) {
        val intent = Intent(GeofenceTabActivity.context, CreateGeofenceActivity::class.java)
        intent.putExtra("update", true)
        intent.putExtra("geofence", fenceZones[circle.id])
        startActivity(intent)
    }

    private fun loadData(filter: String = "", loadMap: Boolean = true) {
        Thread {
            repo = Repository(GeofenceTabActivity.context)
            val fences = repo.db.fenceDao().fetchAll()
            GeofenceTabActivity.context.runOnUiThread {
                if(loadMap) {
                    mMap.clear()
                    if (!fences.isEmpty()) {
                        val circle = fences.map {
                            val colour = if(it.isSafe) CreateGeofenceActivity.GEOFENCE_COLOUR else CreateGeofenceActivity.GEOFENCE_COLOUR_NOT_SAFE
                            val option = CircleOptions().apply {
                                clickable(true)
                                center(LatLng(it.latitude, it.longitude))
                                radius(it.radius.toDouble())
                                fillColor(colour)
                                strokeColor(colour)
                            }
                            val circle = mMap.addCircle(option)
                            fenceZones.put(circle.id, it)
                            circle
                        }.first()
                        circle?.run { mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(center, 13f)) }
                    }
                }
            }
        }.start()
    }

}
