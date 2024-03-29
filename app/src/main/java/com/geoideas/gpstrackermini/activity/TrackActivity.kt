package com.geoideas.gpstrackermini.activity

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.format.DateUtils
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.preference.PreferenceManager
import com.geoideas.gpstrackermini.R
import com.geoideas.gpstrackermini.activity.listeners.SharedPreferenceListener
import com.geoideas.gpstrackermini.activity.util.ActivityUtils
import com.geoideas.gpstrackermini.coms.CommandExecutor
import com.geoideas.gpstrackermini.location.Locator
import com.geoideas.gpstrackermini.map.LiveTrackMap
import com.geoideas.gpstrackermini.util.PermissionsUtil
import com.geoideas.gpstrackermini.map.LocationManager
import com.geoideas.gpstrackermini.map.MapUtils
import com.geoideas.gpstrackermini.map.TrackManager
import com.geoideas.gpstrackermini.repository.Repository
import com.geoideas.gpstrackermini.repository.room.entity.Point
import com.geoideas.gpstrackermini.service.WhereProcessor
import com.geoideas.gpstrackermini.util.AppConstant
import com.geoideas.gpstrackermini.util.PreferenceUtil
import com.geoideas.gpstrackermini.util.SerialList
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial
import com.mikepenz.materialdrawer.holder.StringHolder
import com.mikepenz.materialdrawer.iconics.withIcon
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import com.mikepenz.materialdrawer.model.ProfileDrawerItem
import com.mikepenz.materialdrawer.model.interfaces.withEmail
import com.mikepenz.materialdrawer.model.interfaces.withIcon
import com.mikepenz.materialdrawer.widget.AccountHeaderView
import com.mikepenz.materialdrawer.widget.MaterialDrawerSliderView
import java.io.File
import java.math.RoundingMode
import java.text.DecimalFormat

class TrackActivity : AppCompatActivity(), OnMapReadyCallback {

    private val TAG = TrackActivity::class.java.simpleName
    private val TRACK_CREATION = 1
    private var polylineClicked = true
    private var isGradient = false
    private var max = 0
    private var from = ""
    private var to = ""
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var mMap: GoogleMap
    private lateinit var trackM: TrackManager
    private lateinit var map: LocationManager
    private lateinit var chartFab: FloatingActionButton
    private lateinit var settingFab: FloatingActionButton
    private lateinit var statsFab: FloatingActionButton
    private lateinit var shareButton: ImageButton
    private lateinit var trackSmoothDialog: AlertDialog
    private lateinit var exportDialog: AlertDialog
    private lateinit var rootLayout: View
    private lateinit var slider: MaterialDrawerSliderView
    private lateinit var loader: ProgressBar
    private lateinit var levelBar: ProgressBar
    private lateinit var liveSwitch: SwitchCompat
    private lateinit var liveInfo: TextView
    private val defaultLiveInfo = "Spd(kmph) - : Avg - : Max";

    private lateinit var points: List<Point>

    private val utils = ActivityUtils()
    private lateinit var repo: Repository
    private var trackFilter: Bundle? = null

    lateinit var APP_NAME: String

    val LOCATION_PERMISSIONS = 1
    val FILE_PERMISSIONS = 3
    var hasLocation = false
    var hasFile = false

    val LOCATION_MODE_MESSAGE = "Try updating your location mode to Battery Saving or High Accuracy for increased accuracy."

    private lateinit var serviceDialog: androidx.appcompat.app.AlertDialog
    private lateinit var locationPreferenceDialog: androidx.appcompat.app.AlertDialog
    private lateinit var locationPermissionDialog: androidx.appcompat.app.AlertDialog
    private lateinit var onlyGPSModeDialog: androidx.appcompat.app.AlertDialog
    private lateinit var firstRunDialog: androidx.appcompat.app.AlertDialog

    private lateinit var prefListener: SharedPreferenceListener
    private lateinit var prefs: SharedPreferences
    private lateinit var prefsUtil: PreferenceUtil

    private lateinit var commander: CommandExecutor

    private lateinit var liveTrackMap: LiveTrackMap

    companion object {
        val EXPORT_MESSAGE = "This feature is not accessible with this version of Free GPS Tracker."
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MobileAds.initialize(this) {}
        APP_NAME = resources.getString(R.string.app_name)
        setContentView(R.layout.activity_track)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        repo = Repository(this)
        rootLayout = findViewById(R.id.root)
        slider = findViewById(R.id.slider)
        chartFab = findViewById(R.id.fab_chart)
        settingFab = findViewById(R.id.fab_setting)
        statsFab = findViewById(R.id.fab_stats)
        statsFab.visibility = View.INVISIBLE
        loader = findViewById(R.id.loader)
        shareButton = findViewById(R.id.btn_share)
        liveInfo = findViewById(R.id.text_speed_info)
        liveInfo.text = defaultLiveInfo
        liveSwitch = findViewById(R.id.switch_live_active)
        liveSwitch.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked) runLiveMap() else endLiveMap()
        }

        createDrawer()
        restore(savedInstanceState)

        PreferenceManager.setDefaultValues(this, R.xml.root_preferences, false)
        prefListener = SharedPreferenceListener(this)
        prefs = PreferenceManager.getDefaultSharedPreferences(this).apply {
            registerOnSharedPreferenceChangeListener(prefListener)
        }
        prefsUtil = PreferenceUtil(this)

        prefs.registerOnSharedPreferenceChangeListener(prefListener)

        commander = CommandExecutor(this, Locator(this), Repository(this))

        showOnFirstLaunch()
        createDialogs()
        resolveRequestPermission()
    }

    override fun onResume() {
        super.onResume()
        prefsUtil = PreferenceUtil(this)
        // resolvePermissions()
        showServiceBar()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.run {
            val mapData = Bundle().apply {
                val center = mMap.cameraPosition.target
                putDouble("latitude", center.latitude)
                putDouble("longitude", center.longitude)
                putFloat("zoom", mMap.cameraPosition.zoom)
                putInt("filter", levelBar.progress)
            }

            trackFilter?.putAll(mapData) ?: mapData.run { trackFilter = this }
            putBundle("restoreStructure", trackFilter)
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
        map = LocationManager(mMap, this)
        trackM = TrackManager(mMap, this)
        mMap.setOnPolylineClickListener(::polylineClick)
        trackFilter?.run {
            if (this.containsKey("from")) generateTrack(Intent().putExtras(this))
            val position = CameraPosition(
                LatLng(getDouble("latitude"), getDouble("longitude")),
                getFloat("zoom"),
                0f,
                0f
            )
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(position))
        }
        loadIntent()
        if(PermissionsUtil.hasLocationPermission(this)) {
            mMap.isMyLocationEnabled = true
            runLiveMap()
        }
    }

/*
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_track_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when(item?.itemId ?: -1) {
            R.id.action_export -> export()
            else -> super.onOptionsItemSelected(item)
        }
    }
*/

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == TRACK_CREATION && resultCode == Activity.RESULT_OK)
            generateTrack(data)
    }

    fun createDrawer() {
        val tracks = PrimaryDrawerItem().apply {
            identifier = 2
            name = StringHolder("Geofences")
            withIcon(GoogleMaterial.Icon.gmd_timeline)
            onDrawerItemClickListener = { _, _, _ ->
                toGeofences(rootLayout)
                false
            }
        }

        val location = PrimaryDrawerItem().apply {
            identifier = 4
            name = StringHolder("Location")
            withIcon(GoogleMaterial.Icon.gmd_my_location)
            onDrawerItemClickListener = { _, _, _ ->
                toLocation(rootLayout)
                false
            }
        }

        val tutorials = PrimaryDrawerItem().apply {
            identifier = 5
            name = StringHolder("Tutorials")
            withIcon(GoogleMaterial.Icon.gmd_info)
            onDrawerItemClickListener = { _, _, _ ->
                toTutorial(rootLayout)
                false
            }
        }

        val settings = PrimaryDrawerItem().apply {
            identifier = 7
            name = StringHolder("Settings")
            withIcon(GoogleMaterial.Icon.gmd_settings)
            onDrawerItemClickListener = { _, _, _ ->
                toSettings(rootLayout)
                false
            }
        }

        val plus = PrimaryDrawerItem().apply {
            identifier = 6
            name = StringHolder("GPS Tracker Plus")
            withIcon(GoogleMaterial.Icon.gmd_file_download)
        }

        slider.itemAdapter.add(
            tracks,
            location,
            tutorials,
            settings
            // plus
        )

        AccountHeaderView(this).apply {
            attachToSliderView(slider)
            addProfiles(
                ProfileDrawerItem().apply {
                    name = StringHolder(resources.getString(R.string.app_name))
                    withEmail("teamgeoideas@gmail.com")
                    withIcon(getResources().getDrawable(R.mipmap.ic_launcher))
                    setBackgroundColor(getColor(R.color.colorPrimary))
                }
            )
            dividerBelowHeader = true
        }

    }

    fun toUsers(view: View){
        val intent = Intent(this, UserActivity::class.java)
        startActivity(intent)
    }

    fun toLocation(view: View){
        val intent = Intent(this, LocationActivity::class.java)
        startActivity(intent)
    }

    fun toTrack(view: View){
        val intent = Intent(this, GeofenceTabActivity::class.java)
        startActivity(intent)
    }

    fun toGeofences(view: View){
        val intent = Intent(this, GeofenceTabActivity::class.java)
        startActivity(intent)
    }

    fun toAbout(view: View) {
        val url = AppConstant.APP_WEBSITE
        val i = Intent(Intent.ACTION_VIEW)
        i.data = Uri.parse(url)
        startActivity(i)
    }

    fun toTutorial(view: View) {
        val url = AppConstant.APP_WEBSITE_TUTORIAL
        val i = Intent(Intent.ACTION_VIEW)
        i.data = Uri.parse(url)
        startActivity(i)
    }

    fun toSettings(view: View) {
        startActivity(Intent(this, SettingsActivity::class.java))
    }

    fun createTrack(view: View) =
        startActivityForResult(Intent(this, TrackFilterActivity::class.java), TRACK_CREATION)

    fun recalculateTrack(view: View) {
        trackSmoothDialog.show()
    }

    fun createChart(view: View) {
        if(isGradient) {
            val intent = Intent(this, SpeedChartActivity::class.java).apply {
                putExtra("speeds", trackM.allSpeeds().toFloatArray())
                putExtra("durations", trackM.allDurations().toLongArray())
                putExtra("max", max)
                putExtra("label", "$from - $to")
            }
            startActivity(intent)
        }
    }

    fun dismissSmoothDialog(view: View) {
        trackSmoothDialog.dismiss()
    }

    fun applyTrackSmooth(view: View) {
        trackSmoothDialog.dismiss()
        loadTrack()
    }

    fun dismissShareDialog(view: View) {
        exportDialog.dismiss()
    }

    fun createStats(view: View) {
        if(!::trackM.isInitialized || trackM.allSpeeds().isNullOrEmpty()) return

        val speeds = trackM.allSpeeds()
        val oneDP = { num: Number ->
            val df = DecimalFormat("#.#")
            df.roundingMode = RoundingMode.CEILING
            df.format(num)
        }

        val threeDP = { num: Number ->
            val df = DecimalFormat("#.###")
            df.roundingMode = RoundingMode.CEILING
            df.format(num)
        }

        val twoDP = { num: Float? ->
            val df = DecimalFormat("#.##")
            df.roundingMode = RoundingMode.CEILING
            df.format(num)
        }

        val viewDialog = layoutInflater.inflate(R.layout.popup_track_stats, null)
        val duration = trackM.allDurations().sum()
        val distance = trackM.distance()
        val avgSpeed = ((distance/duration)*3600)/1000f
        val dialog = AlertDialog.Builder(this).run {
            viewDialog.findViewById<TextView>(R.id.text_duration).apply {
                text = DateUtils.formatElapsedTime(duration)
            }
            viewDialog.findViewById<TextView>(R.id.text_distance).apply {
                text = twoDP(distance/1000f) +" km"
            }
            viewDialog.findViewById<TextView>(R.id.text_average_speed).apply {
                text = twoDP(avgSpeed) + " kmph"
            }

            viewDialog.findViewById<TextView>(R.id.text_max_speed).apply {
                text = twoDP(speeds.max()).toString()+ " kmph"
            }
            setView(viewDialog)
            create()
        }
        dialog.show()
        viewDialog.findViewById<Button>(R.id.btn_okay).apply {
            setOnClickListener { dialog.dismiss() }
        }
    }

    private fun restore(state: Bundle?) {
        state?.run {
            trackFilter = getBundle("restoreStructure")
        }
    }

    private fun loadIntent() {
        val extras = intent?.extras ?: return
        isGradient = extras?.getBoolean("gradient") ?: return
        max = extras?.getInt("max") ?: return
        utils.startStatusBar(loader)
        Thread {
            points = extras?.get("points") as List<Point>? ?: pointsString() ?: listOf()
            runOnUiThread {
                utils.stopStatusBar(loader)
                createTrack(points)
            }
        }.start()
    }

    private fun pointsString() : List<Point> {
        val rCode = intent?.extras?.getInt("rCode") ?: return listOf()
        val qc = repo.db.queryCycleDao().findByRCode(rCode) ?: return listOf()
        val gson = Gson()
        val responseObj = gson.fromJson(qc.response, JsonObject::class.java)
        val pointsString = responseObj.getAsJsonPrimitive("response").asString
        if(pointsString.isEmpty()) return listOf()
        val pointsArray = JsonParser().parse(pointsString) as JsonArray
        val points = pointsArray.map { Gson().fromJson(it, Point::class.java) }
        return SerialList<Point>().apply { addAll(points) }
    }

    private fun generateTrack(data: Intent?) {
        liveSwitch.isChecked = false
        trackM.clearTracks()
        val query = data?.extras ?: return
        trackFilter = query
        from = query.getString("from")
        to = query.getString("to")
        val accuracy = query.getDouble("accuracy")
        isGradient = query.getBoolean("gradient")
        max = query.getInt("max")
        levelBar.progress = query.getInt("filter")
        utils.startStatusBar(loader)
        Thread {
            val dao = Repository(this).db.pointDao()
            points = if(accuracy > 0) dao.fetchBetweenAccuracy(from, to, accuracy) else  dao.fetchBetween(from, to)
            //var visiblePoints = if(isGradient) trackM.withinBounds(points, bounds) else listOf()
            this.runOnUiThread { loadTrack() }
        }.start()
    }

    private fun createTrack(points: List<Point>, visiblePoints: List<Point> = listOf()) {
        if(points.isEmpty()) return
        statsFab.show()
        if(isGradient) {
            trackM.createGradientTrack(points, visiblePoints, "Track", max, levelBar.progress, this, loader) {}
            showGradientButtons()
        }
        else {
            hideGradientButtons()
            trackM.createTrack(points, "Track")
        }
    }

    private fun loadTrack() {
        utils.startStatusBar(loader)
        trackM.clearTracks()
        createTrack(points)
        utils.stopStatusBar(loader)
    }

    private fun hideGradientButtons() {
        chartFab.hide()
        settingFab.hide()
    }

    private fun showGradientButtons() {
        chartFab.show()
        settingFab.show()
    }

    private fun polylineClick(line: Polyline) {
        polylineClicked = true
        if(isGradient) trackM.showSegmentInfo(line)
        else trackM.showTitle()
        Handler(Looper.getMainLooper()).postDelayed( { polylineClicked = false }, 3000)
    }

    private fun refreshTracks() {
        utils.startStatusBar(loader)
        val bounds = MapUtils.mapBounds(trackM.map);
        Thread {
            val visiblePoints = trackM.withinBounds(points, bounds)
            this.runOnUiThread {
                if(visiblePoints.size == points.size) utils.stopStatusBar(loader)
                else {
                    trackM.clearTracks()
                    createTrack(points, visiblePoints)
                    utils.stopStatusBar(loader)
                }
            }
        }.start()
    }

    private fun createTrackSmoothDialog() : AlertDialog {
        val inflater = this.layoutInflater
        return AlertDialog.Builder(this).run {
            val content = inflater.inflate(R.layout.popup_track_setting, null)
            levelBar = content.findViewById<SeekBar>(R.id.seekBar_filter_level)
            setView(content)
            create()
        }
    }

    private fun createExportDialog(file: File, saveLocation: String) : AlertDialog {
        val inflater = this.layoutInflater
        return AlertDialog.Builder(this).run {
            val content = inflater.inflate(R.layout.popup_track_share, null)
            content.findViewById<TextView>(R.id.text_filename).apply {
                append(saveLocation.split(">")[1])
            }
            content.findViewById<Button>(R.id.btn_track_export_share).apply {
                setOnClickListener { shareExport(file) }
            }
            setView(content)
            create()
        }
    }

    private fun export(): Boolean {
        if (PermissionsUtil.hasFilePermission(this)) {
            if (!::points.isInitialized || points.isEmpty()) {
                Toast.makeText(this, "No track line present", Toast.LENGTH_LONG).show()
                return true
            }
            utils.startStatusBar(loader)
            if (isGradient)
                trackM.exportGradientTrack(::onExportComplete)
            else trackM.exportTrack(::onExportComplete)
        } else {
            resolveFilePermission()
        }
        return true
    }

    private fun onExportComplete(file: File, location: String) {
        runOnUiThread {
            val f = File(file.absolutePath)
            val uri = Uri.fromFile(f)
            Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).also {
                it.data = uri
                sendBroadcast(it)
            }
            utils.stopStatusBar(loader)
            exportDialog = createExportDialog(file, location)
            exportDialog.show()
        }
    }

    private fun shareExport(file: File) {
        val viewIntent = Intent(Intent.ACTION_SEND).apply {
            setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            val path = FileProvider.getUriForFile(
                this@TrackActivity,
                "com.geoideas.gpstrackermini.fileprovider",
                file
            )
            type = "text/*"
            putExtra(Intent.EXTRA_STREAM, path)
        }
        exportDialog.dismiss()
        startActivity(viewIntent)
    }

    private fun updateLiveInfo(currentSpeed: Int, avgSpeed: Int, maxSpeed: Int) {
        var message = "(kmph)Spd - $currentSpeed: Avg - $avgSpeed: Max - $maxSpeed"
        liveInfo.text = message
        // Log.d(TAG, message)
    }

    private fun runLiveMap() {
        if(!this::liveTrackMap.isInitialized && liveSwitch.isChecked) {
            liveTrackMap = LiveTrackMap(mMap, this)
            liveTrackMap.onUpdate = ::updateLiveInfo
        }
        liveTrackMap.restart()
    }

    private fun endLiveMap() {
        if(this::liveTrackMap.isInitialized && !liveSwitch.isChecked) {
            liveTrackMap.terminate()
            liveInfo.text = defaultLiveInfo
        }
    }

    fun showDrawer(view: View) {
        slider.drawerLayout?.openDrawer(slider)
    }

    fun closeServiceDialog(view: View) {
        serviceDialog.dismiss()
        showServiceBar()
        resolvePermissions()
    }

    fun activateServices(view: View) {
        activateServices()
    }

    fun activateServices() {
        PreferenceManager.getDefaultSharedPreferences(this).edit().run {
            if(PermissionsUtil.hasLocationPermission(this@TrackActivity)) {
                if(utils.isLocationOn(this@TrackActivity)) {
                   if(utils.isHighAccuracyMode(this@TrackActivity)) {
                       putBoolean("location_service", true)
                       putBoolean("geofence_alert", true)
                       apply()
                   } else {
                       onlyGPSModeDialog.dismiss()
                       onlyGPSModeDialog.show()
                       showServiceBar()
                   }
                } else {
                    locationPreferenceDialog.dismiss()
                    locationPreferenceDialog.show()
                    showServiceBar()
                }
            } else {
                locationPermissionDialog.dismiss()
                locationPermissionDialog.show()
                showServiceBar()
            }
        }
    }

    fun export(view: View) {
        export()
    }

    private fun showOnFirstLaunch() {
        if(!prefs.contains("first")) {
            firstRunDialog = androidx.appcompat.app.AlertDialog.Builder(this, R.style.AlertDialogTheme).run {
                setTitle("Tutorials")
                setMessage("Click the Tutorial maximize your experience. Tutorials can me found under the menu in the top-left corner")
                setNegativeButton("Close") { d, _ ->  d.dismiss()}
                setPositiveButton("Tutorial") { _, _ ->
                    toTutorial(rootLayout)
                    firstRunDialog.dismiss()
                }
                create()
            }
            firstRunDialog.show()
            val editor = prefs.edit()
            editor.putBoolean("first", false)
            editor.apply()
        }
    }

    private fun createServiceDialog() {
        val view = layoutInflater.inflate(R.layout.popup_service_state, null)
        val grantBox = view.findViewById<CheckBox>(R.id.grant_box)
        val onBox = view.findViewById<CheckBox>(R.id.on_box)
        val modeBox = view.findViewById<CheckBox>(R.id.mode_box)
        grantBox.isChecked = PermissionsUtil.hasLocationPermission(this)
        onBox.isChecked = utils.isLocationOn(this)
        modeBox.isChecked = utils.isHighAccuracyMode(this)
        serviceDialog = androidx.appcompat.app.AlertDialog.Builder(this, R.style.AlertDialogTheme).run {
            setTitle("Services Offline - Complete the following")
            setView(view)
            setNegativeButton("Continue") { _, _ -> closeServiceDialog(rootLayout) }
            setCancelable(false)
            create()
        }
    }

    private fun createDialogs() {
        createServiceDialog()
        trackSmoothDialog = createTrackSmoothDialog()

        locationPreferenceDialog = androidx.appcompat.app.AlertDialog.Builder(this, R.style.AlertDialogTheme).run {
            setTitle("Device Location Off")
            setMessage(
                APP_NAME +" needs the ability to access your location.\n" +
                        "You are seeing this notice because of current Setting."
            )
            setNegativeButton("Close") { d, _ -> d.dismiss() }
            setPositiveButton("Turn On") { _, _ ->
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            setCancelable(false)
            create()
        }

        locationPermissionDialog = androidx.appcompat.app.AlertDialog.Builder(this, R.style.AlertDialogTheme).run {
            setTitle("Grant Location Permission")
            setMessage(
                APP_NAME +" needs the ability to access your location.\n" +
                        "This is to support all mapping services."
            )
            setNegativeButton("Close") { d, _ -> d.dismiss() }
            setPositiveButton("Turn On") { _, _ -> resolveLocationPermission()}
            setCancelable(false)
            create()
        }

        onlyGPSModeDialog = utils.createLocationModeChangeDialog(this, LOCATION_MODE_MESSAGE)
    }

    private fun showServiceBar() {
        val prefsEdit = PreferenceManager.getDefaultSharedPreferences(this@TrackActivity).edit()
        val hasAllLocationPermissions = hasAllLocationPermissions()
        if(!hasAllLocationPermissions)
            prefsEdit.putBoolean("location_service", false)
                .putBoolean("geofence_alert", false)
                .commit()
        val ls = prefs.getBoolean("location_service", false)
        val ga = prefs.getBoolean("geofence_alert", false)
        if(!ls && !ga)
            Snackbar.make(rootLayout, "Services are offline", Snackbar.LENGTH_INDEFINITE).apply {
                setAction("Turn On") {

                    if(hasAllLocationPermissions) {
                        prefsEdit.putBoolean("location_service", true)
                        prefsEdit.putBoolean("geofence_alert", true)
                        prefsEdit.apply()
                    } else {
                        createServiceDialog()
                        serviceDialog.show()
                    }
                }
                show()
            }
        if(ls || ga) {
            if (ls) startWhereService("location_service")
            if (ga) startWhereService()
        }
    }

    private fun resolveLocationPrefernces() {
        val locationPreferences = prefsUtil.hasGeofenceAlert() ||
                prefsUtil.hasLocationService()
        if (locationPreferences && !utils.isLocationOn(this)) {
            locationPreferenceDialog.dismiss()
            locationPreferenceDialog.show()
        }
    }

    private fun resolvePermissions() {
        if(!PermissionsUtil.hasLocationPermission(this)) {
            locationPermissionDialog.dismiss()
            locationPermissionDialog.show()
        }
        if(!utils.isLocationOn(this)) {
            locationPreferenceDialog.dismiss()
            locationPreferenceDialog.show()
        }
        else if(!utils.isHighAccuracyMode(this)) {
            onlyGPSModeDialog.dismiss()
            onlyGPSModeDialog.show()
        }
        resolveLocationPrefernces()
    }

    private fun resolveLocationPermission() {
        val perms = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        hasLocation = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (!hasLocation) {
            ActivityCompat.requestPermissions(
                this,
                perms,
                LOCATION_PERMISSIONS
            )
        }
    }

    private fun resolveFilePermission() {
        val perms = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        hasFile = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        if (!hasFile) {
            ActivityCompat.requestPermissions(
                this,
                perms,
                FILE_PERMISSIONS
            )
        }
    }

    private fun resolveRequestPermission() {
        intent?.extras?.getBoolean(PermissionsUtil.MISSING_PERMISSION)?.run {
            if (this) {
                with(PermissionsUtil) {
                    when (intent?.extras?.getCharSequence(GET_PERMISSION)) {
                        LOCATION_PERMISSION -> resolveLocationPermission()
                        FILE_PERMISSION -> resolveFilePermission()
                    }
                }
            }
        }
    }

    private fun hasAllLocationPermissions() =
        PermissionsUtil.hasLocationPermission(this) &&
        utils.isLocationOn(this) &&
        utils.isHighAccuracyMode(this)


    private fun startService() {
        if (hasLocation) startWhereService()
    }

    private fun startWhereService(preference: String) {
        if(hasAllLocationPermissions())
            if(!(WhereProcessor.TRACKING))
                Intent(this, WhereProcessor::class.java).also {
                    it.putExtra(AppConstant.PREFERENCE_CHANGED, true)
                    it.putExtra("preference", preference)
                    ContextCompat.startForegroundService(this, it)
                }
    }

    private fun startWhereService() {
        if(hasAllLocationPermissions())
            Intent(this, WhereProcessor::class.java).also {
                ContextCompat.startForegroundService(this, it)
            }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        hasLocation = requestCode == LOCATION_PERMISSIONS && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
        hasFile = requestCode == FILE_PERMISSIONS && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED

    }


}
