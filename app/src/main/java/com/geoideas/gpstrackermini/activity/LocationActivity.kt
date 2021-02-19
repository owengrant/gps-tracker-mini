package com.geoideas.gpstrackermini.activity

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.*
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.geoideas.gpstrackermini.R
import com.geoideas.gpstrackermini.activity.util.ActivityUtils
import com.geoideas.gpstrackermini.util.PermissionsUtil
import com.geoideas.gpstrackermini.location.Locator
import com.geoideas.gpstrackermini.map.LineString
import com.geoideas.gpstrackermini.map.LocationManager
import com.geoideas.gpstrackermini.map.Point
import com.geoideas.gpstrackermini.util.AppConstant
import com.geoideas.gpstrackermini.util.GoogleMapHelper
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import java.io.File
import java.io.FileOutputStream
import java.util.*


class LocationActivity : AppCompatActivity(), OnMapReadyCallback {
    public val TAG = LocationActivity::class.java.simpleName

    private enum class Feature { NONE, POINT, LINE, POLYGON }

    private val util = ActivityUtils()
    private lateinit var loc: Locator
    private lateinit var mMap: GoogleMap
    private lateinit var map: LocationManager
    private lateinit var view: View
    private lateinit var pointManager: LinearLayout
    private lateinit var pointCreator: LinearLayout
    private lateinit var title: EditText
    private lateinit var loader: ProgressBar
    private lateinit var instruction: TextView
    private var selectedFeature = Feature.NONE
    private var currMarker: Marker? = null
    private var nextMarker: Marker? = null
    private var currLine: Polyline? = null
    private var nextLine: Polyline? = null
    private var firstLinePoint: Marker? = null
    private var currPolygon: Polygon? = null
    private var nextPolygon: Polygon? = null
    private var firstPolygonPoint: Marker? = null

    private lateinit var interstitialAd: InterstitialAd

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        loc = Locator(this)

        view = findViewById(R.id.layout_location)
        pointManager = findViewById<LinearLayout>(R.id.layout_point_manager)
        pointCreator = findViewById<LinearLayout>(R.id.layout_point_creator)
        title = findViewById<EditText>(R.id.input_point_title)
        loader = findViewById<ProgressBar>(R.id.loader)
        instruction = findViewById(R.id.text_instruction)

        interstitialAd = util.initAds(this);
        util.loadAd(interstitialAd)
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
        if(PermissionsUtil.resolveLocationPermission(this)) {
            mMap.isMyLocationEnabled = true
            mMap.setOnMarkerClickListener(::onMarkerClicked)
            mMap.setOnMapLongClickListener(::onLongClick)
            mMap.setOnPolylineClickListener(::onLineClicked)
            map = LocationManager(mMap, this)
            loadIntent()
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_location, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when(item?.itemId ?: -1){
            R.id.action_add_point -> {
                selectedFeature = Feature.POINT
                showInstruction()
                true
            }
            R.id.action_add_line -> {
                selectedFeature = Feature.LINE
                showInstruction()
                true
            }
            R.id.action_screenshot-> {
                createScreenShot()
                true
            }
            R.id.action_share_location -> {
                shareLocation()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showInstruction(show: Boolean = true) {
        instruction.visibility = if(show) View.VISIBLE else View.INVISIBLE
    }

    private fun loadIntent() {
        val position = intent?.extras?.getString("position") ?: return
        val label = intent?.extras?.getString("title") ?: return
        val lngLat = stringToList(position).map { it.toDouble() }
        val latLng = LatLng(lngLat[1], lngLat[0])
        title.text.clear()
        title.text.append(label)
        createPoint(latLng, label)
        map.moveCamera(latLng, 18f)
    }

    private fun stringToList(s: String) =
        s.substring(1, s.length-1)
        .split(",")
        .map { it.trim() }

    private fun onMarkerClicked(it: Marker): Boolean {
        currMarker = it
        showView(pointManager)
        if(selectedFeature == Feature.NONE) {
            if(firstLinePoint?.id ?: -11 == it.id) return false
            selectedFeature = Feature.POINT
        }
        return false
    }

    private fun onLineClicked(it: Polyline) {
        if(selectedFeature == Feature.NONE) {
            currLine = it
            selectedFeature = Feature.LINE
            showView(pointManager)
            map.findLine(it.id)?.midPoint?.showInfoWindow()
        }
    }

    private fun onLongClick(it: LatLng) {
        title.text.clear()
        nextMarker?.remove()
        when(selectedFeature) {
            Feature.POINT -> addPoint(it)
            Feature.LINE -> addLine(it)
            Feature.POLYGON -> addPolygon(it)
        }
    }

    private fun addPoint(latlng: LatLng) {
        val options = MarkerOptions().position(latlng)
        nextMarker = mMap.addMarker(options)
        showView(pointCreator)
    }

    private fun addLine(latlng: LatLng) {
        val options = PolylineOptions().apply {
            color(Color.BLUE)
            width(5f)
            add(latlng)
            clickable(true)
        }
        if(firstLinePoint != null){
            firstLinePoint?.remove()
            firstLinePoint = null
        }
        if(nextLine != null){
            nextLine!!.remove()
            options.addAll(nextLine?.points ?: listOf())
            nextLine = mMap.addPolyline(options)
        }
        else{
            firstLinePoint = mMap.addMarker(MarkerOptions().position(latlng))
            nextLine = mMap.addPolyline(options)
        }
        showView(pointCreator)
    }

    private fun addPolygon(latlng: LatLng) {
        val options = PolygonOptions().apply {
            add(latlng)
            clickable(true)
        }
        if(firstPolygonPoint != null){
            firstPolygonPoint?.remove()
            firstPolygonPoint = null
        }
        if(nextPolygon != null){
            nextPolygon!!.remove()
            options.addAll(nextPolygon?.points ?: listOf())
            nextPolygon = mMap.addPolygon(options)
        }
        else{
            firstPolygonPoint = mMap.addMarker(MarkerOptions().position(latlng))
            nextPolygon = mMap.addPolygon(options)
        }
        showView(pointCreator)
    }

    private fun endPointCreate() {
        when(selectedFeature){
            Feature.POINT -> {
                nextMarker?.remove()
                nextMarker = null
            }
            Feature.LINE -> {
                firstLinePoint?.remove()
                firstLinePoint = null
                nextLine?.remove()
                nextLine = null
            }
        }
        resetPoint(pointCreator)
    }


    fun create(view: View) {
        when(selectedFeature) {
            Feature.POINT -> createPoint(nextMarker?.position, title.text.toString())
            Feature.LINE -> createLine()
        }
        endPointCreate()
    }

    fun cancel(view: View) = endPointCreate()

    fun close(view: View) = resetPoint(pointManager)

    fun delete(view: View) {
        when(selectedFeature) {
            Feature.POINT -> deletePoint()
            Feature.LINE -> deleteLine()
        }
        resetPoint(pointManager)
    }

    private fun createPoint(latLng: LatLng?, title: String) {
        latLng ?: return
        val options = MarkerOptions()
            .position(latLng)
            .title(title)
        map.addPoint(Point(options = options))
    }

    private fun createLine() {
        val points = nextLine?.points ?: return
        if(points.size < 2) return
        val options = PolylineOptions().apply {
            color(Color.BLUE)
            width(5f)
            addAll(points)
            clickable(true)
        }
        map.addLine(LineString(options = options),title = title?.text.toString())
    }

    private fun deletePoint() {
        if(map.removePoint(currMarker?.id ?: "")) currMarker = null
    }

    private fun deleteLine() {
        if(map.removeLine(currLine?.id ?: "")) nextLine = null
    }


    private fun hideView(view: View) {
        view.setVisibility(View.GONE)
        showInstruction(false)
    }

    private fun showView(view: View) {
        hideView(pointManager)
        hideView(pointCreator)
        view.visibility = View.VISIBLE
    }

    private fun resetPoint(view: View) {
        selectedFeature = Feature.NONE
        hideView(view)
    }

    private fun createScreenShot() {
        if(PermissionsUtil.resolveFilePermission(this)) {
            map.showInfo()
            mMap.snapshot { image ->
                try {
                    val docFolder =
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                    val root = File(docFolder.parent + "/" + AppConstant.APP_NAME)
                    if (!root.exists()) root.mkdir()
                    val imageFile = File(root, "GIS SMS${Random().nextInt(10000)}.jpg").absoluteFile
                    if (imageFile.exists()) imageFile.delete()
                    val out = FileOutputStream(imageFile)
                    image.compress(Bitmap.CompressFormat.JPEG, 90, out)
                    out.flush()
                    out.close()
                    val path = FileProvider.getUriForFile(
                        this,
                        "com.geoideas.gpstracker.fileprovider",
                        imageFile
                    )
                    val f = File(imageFile.absolutePath)
                    val uri = Uri.fromFile(f)
                    Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).also {
                        it.data = uri
                        sendBroadcast(it)
                    }
                    val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                        setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        setDataAndType(path, "image/*")
                    }
                    startActivity(viewIntent)
                } catch (e: Exception) {
                    e.printStackTrace()
                    util.showSnackBar(view, "Image save error")
                } finally {
                    map.hideInfo()
                }
            }
        }
    }

    private fun shareLocation() {
        if(util.hasLocationPermission(this)){
            loader.visibility = View.VISIBLE
            loc.currentLocation {
                loader.visibility = View.GONE
                val task = Intent(Intent.ACTION_SEND)
                    .setType("text/plain")
                    .putExtra(Intent.EXTRA_TEXT, GoogleMapHelper.locationURLText(it))
                val intent = Intent.createChooser(task, "Share with")
                util.showAd(interstitialAd)
                startActivity(intent)
            }
        }
        else startActivity(Intent(this, TrackActivity::class.java))
    }

}
