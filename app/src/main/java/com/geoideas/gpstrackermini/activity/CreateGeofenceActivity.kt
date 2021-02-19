package com.geoideas.gpstrackermini.activity

import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.geoideas.gpstrackermini.R
import com.geoideas.gpstrackermini.activity.util.ActivityUtils
import com.geoideas.gpstrackermini.util.PermissionsUtil
import com.geoideas.gpstrackermini.location.Locator
import com.geoideas.gpstrackermini.map.LocationManager
import com.geoideas.gpstrackermini.repository.Repository
import com.geoideas.gpstrackermini.repository.room.entity.Fence
import com.geoideas.gpstrackermini.repository.room.entity.FenceUserAuth
import com.geoideas.gpstrackermini.repository.room.entity.User
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.gson.Gson
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.thread

class CreateGeofenceActivity : AppCompatActivity(), OnMapReadyCallback {

    private val TAG = CreateGeofenceActivity::class.java.simpleName

    private val utils = ActivityUtils()
    private lateinit var mMap: GoogleMap
    private lateinit var map: LocationManager
    private lateinit var loc: Locator
    private lateinit var title: EditText
    private lateinit var description: EditText
    private lateinit var enter: CheckBox
    private lateinit var exit: CheckBox
    private lateinit var dwell: CheckBox
    private lateinit var sunday: CheckBox
    private lateinit var monday: CheckBox
    private lateinit var tuesday: CheckBox
    private lateinit var wednesday: CheckBox
    private lateinit var thursday: CheckBox
    private lateinit var friday: CheckBox
    private lateinit var saturday: CheckBox
    private lateinit var active: CheckBox
    private lateinit var safe: CheckBox
    private lateinit var notify: CheckBox
    private lateinit var btn: Button
    private lateinit var from: EditText
    private lateinit var to: EditText
    private lateinit var helperView: View
    private lateinit var mainView: View
    private lateinit var loader: ProgressBar
    private lateinit var radius: SeekBar
    private lateinit var rootLayout: SlidingUpPanelLayout
    private lateinit var scrollLayout: ScrollView
    private lateinit var arrowImage: ImageView

    private var circle: Circle? = null
    private var updating = false
    private var fence = Fence()
    private lateinit var repo: Repository
    private lateinit var locator: Locator
    private val users = ArrayList<User>()
    private val authZUsers = ArrayList<Int>()

    private val gson = Gson()
    private var restoreStructure: RestoreStructure? = null

    companion object {
        val GEOFENCE_COLOUR_NOT_SAFE = Color.argb(85,250,0,0)
        val GEOFENCE_COLOUR = Color.argb(85,0,250,0)
        val GEOFENCE_COLOUR_NEW = Color.argb(85,0,0,250)
    }

    inner class RestoreStructure(
        val fLat: Double,
        val fLong: Double,
        val radius: Double,
        val cLat: Double,
        val cLong: Double,
        val zoom: Float,
        val selectedUsers: ArrayList<Int>
    )

    private lateinit var interstitialAd: InterstitialAd;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_geofence)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map_fence) as SupportMapFragment
        mapFragment.getMapAsync(this)
        locator = Locator(this)
        repo = Repository(this)
        scrollLayout = findViewById(R.id.scrollView)
        arrowImage = findViewById(R.id.image_arrow)
        rootLayout = findViewById(R.id.sliding_layout)
        rootLayout.addPanelSlideListener(object: SlidingUpPanelLayout.PanelSlideListener {
            override fun onPanelSlide(panel: View?, slideOffset: Float) {
                if(slideOffset > 0)
                    arrowImage.setImageResource(R.drawable.ic_keyboard_arrow_down_black_24dp)
                else arrowImage.setImageResource(R.drawable.ic_keyboard_arrow_up_black_24dp)
            }
            override fun onPanelStateChanged(
                panel: View?,
                previousState: SlidingUpPanelLayout.PanelState?,
                newState: SlidingUpPanelLayout.PanelState?
            ) {}
        })
        radius = findViewById(R.id.seek_radius)
        mainView = findViewById(R.id.layout_create_geofence)
        title = findViewById(R.id.input_fence_title)
        description = findViewById(R.id.input_fence_description)
        sunday = findViewById(R.id.box_fence_sunday)
        monday = findViewById(R.id.box_fence_monday)
        tuesday = findViewById(R.id.box_fence_tuesday)
        wednesday = findViewById(R.id.box_fence_wednesday)
        thursday = findViewById(R.id.box_fence_thursday)
        friday = findViewById(R.id.box_fence_friday)
        saturday = findViewById(R.id.box_fence_saturday)
        active = findViewById(R.id.box_fence_active)
        safe = findViewById(R.id.box_safe)
        notify = findViewById(R.id.box_notify)
        enter = findViewById(R.id.box_fence_enter)
        exit = findViewById(R.id.box_fence_exit)
        dwell = findViewById(R.id.box_fence_dwell)
        btn = findViewById(R.id.btn_geofence_create)
        from = findViewById(R.id.input_from_time)
        to = findViewById(R.id.input_to_time)
        radius.setOnSeekBarChangeListener(onRadiusChanged())
        radius.setProgress(100, true)
        updating = intent.hasExtra("update")
        if(updating) fence = intent.extras.get("geofence") as Fence
        loc = Locator(this)
        loadData()
        restore(savedInstanceState)
        interstitialAd = utils.initAds(this);
        utils.loadAd(interstitialAd)
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        circle?.run {
            RestoreStructure(
                center.latitude,
                center.longitude,
                radius,
                mMap.cameraPosition.target.latitude,
                mMap.cameraPosition.target.longitude,
                mMap.cameraPosition.zoom,
                authZUsers
            ). run { outState?.putCharSequence("restoreStructure", gson.toJson(this)) }
        } ?: return
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
            map = LocationManager(mMap, this)
            if (updating) prepUpdating(fence)
            else {
                restoreStructure?.run {
                    createCircle(LatLng(fLat, fLong), radius)
                    val position = CameraPosition(LatLng(cLat, cLong), zoom, 0f, 0f)
                    mMap.moveCamera(CameraUpdateFactory.newCameraPosition(position))
                }
                mMap.setOnMapLongClickListener {
                    val task = object : TimerTask() {
                        override fun run() {
                            runOnUiThread { createCircle(it, radius.progress.toDouble()) }
                        }
                    }
                    Timer().schedule(task, 200)
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val ui = R.menu.menu_create_geofence
        if(updating) menuInflater.inflate(ui, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when(item?.itemId ?: -1) {
            R.id.menu_geofence_delete -> {
                deleteFence()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    fun doneFence(view: View) { helperView.visibility = View.GONE }

    fun create(view: View) {
        if(utils.onlyGPSMode(this)) utils.showLocationModeChangeDialog(this)
        else {
            if (!validate()) return
            val label = title.text.toString()
            val desc = description.text.toString()
            val latLng = circle!!.center
            //var value = radius.text.toString()
            //var rad = if(value.isNotEmpty()) value.toFloat() else 10f
            val rad = circle?.radius?.toFloat() ?: 100.0f
            val fence = Fence(
                label,
                desc,
                latLng.latitude,
                latLng.longitude,
                rad,
                enter.isChecked,
                exit.isChecked,
                true,
                active.isChecked,
                days(),
                from.text.toString(),
                to.text.toString(),
                notify.isChecked,
                safe.isChecked
            )
            if (updating) {
                fence.id = this.fence.id
                fence.key = this.fence.key
                update(fence)
            } else addGeofence(fence)
        }
    }

    fun fromTime(view: View) = TimePicker()
        .apply { onSelected =  { hour, min -> from.setText(createTime(hour, min)) } }
        .run { show(supportFragmentManager, "fromTimePicker") }

    fun toTime(view: View) = TimePicker()
        .apply { onSelected =  { hour, min -> to.setText(createTime(hour, min)) } }
        .run { show(supportFragmentManager, "toTimePicker") }

    fun showUsersList(view: View) {
        val names = users.map { it.name }.toTypedArray()

        val dialog = this.let {
            AlertDialog.Builder(it).apply {
                setTitle("Select Users To Be Notified")
                setMultiChoiceItems(names, selectedUsers(), DialogInterface.OnMultiChoiceClickListener(::addUser))
                setPositiveButton("Okay", DialogInterface.OnClickListener(::addUsers))
                setNegativeButton("Cancel", DialogInterface.OnClickListener { d, id -> Log.d(TAG, "cancel") })
            }.create().show()
        }
    }

    private fun restore(state: Bundle?) {
        state?.run {
            val restoreStructureStr = getCharSequence("restoreStructure").toString()
            restoreStructure = gson.fromJson(restoreStructureStr, RestoreStructure::class.java).run {
                authZUsers.addAll(selectedUsers)
                this
            }
        }
    }

    private fun onRadiusChanged()  = object: SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
            circle ?: return
            val size = if(p1 <= 10) 10 else p1
            val c = circle
            createCircle(c!!.center, size.toDouble(), false)
        }

        override fun onStartTrackingTouch(p0: SeekBar?) {}

        override fun onStopTrackingTouch(p0: SeekBar?) { }

    }

    private fun addAuthZUsers(fid: Long = fence.id) {
        //remove all users from fence
        val fenceUserAuthDao = repo.db.fenceUserAuthDao()
        fenceUserAuthDao.delete(fid)
        //add all authorized users
        authZUsers
        .map { users[it].id }
        .forEach { fenceUserAuthDao.insert(FenceUserAuth(it, fid)) }
    }

    private fun selectedUsers(): BooleanArray {
        val checked = BooleanArray(users.size)
        for(i in 0..users.size-1)
            checked[i] = authZUsers.contains(i)
        return checked
    }

    private fun addUser(dialog: DialogInterface, id: Int, checked: Boolean) {
        if(checked) authZUsers.add(id)
        else if(authZUsers.contains(id)) authZUsers.remove(id)
        Log.d(TAG, authZUsers.toString())
    }

    private fun addUsers(dialog: DialogInterface, id: Int) {
        Log.d(TAG,"okay")
    }

    private fun createTime(hour: Int, minute: Int) = "${putZero(hour)}:${putZero(minute)}:00"

    private fun putZero(value: Int) = if (value < 10) "0$value" else value.toString()

    private fun update(fence: Fence) {
        val dao = repo.db.fenceDao()
        Thread {
            dao.update(fence)
            addAuthZUsers()
            utils.showSnackBar(mainView, "Geofence Updated")
        }.start()
    }

    private fun loadData() {
        Thread {
            loadUsers()
            loadFences()
        }.start()
    }

    private fun loadUsers() {
        val fenceUserAuthDao = repo.db.fenceUserAuthDao()
        repo.db.userDao().fetchUsers().forEach{ users.add(it) }
        val fenceUsers = fenceUserAuthDao.users(fence.id)
        var count =  0;
        for(fu in fenceUsers)
            for(u in users)
                if(fu.id == u.id) {
                    authZUsers.add(users.indexOf(u))
                    count++
                }
    }

    private fun loadFences() {
        val fences = repo.db.fenceDao().fetchAll()
        runOnUiThread {
            if (!fences.isEmpty()) {
                fences.filter { fence.id != it.id }
                    .forEach {
                    val colour = if(it.isSafe) GEOFENCE_COLOUR else GEOFENCE_COLOUR_NOT_SAFE
                    CircleOptions().run {
                        clickable(true)
                        center(LatLng(it.latitude, it.longitude))
                        radius(it.radius.toDouble())
                        fillColor(colour)
                        strokeColor(colour)
                        mMap.addCircle(this)
                    }
                }
            }
        }
    }

    private fun addGeofence(f: Fence) =
        Locator(this).addGeofence(f).apply {
            val dao = repo.db.fenceDao()
            addOnSuccessListener {
                Thread {
                    dao.insert(f)
                    val last = dao.findLast()
                    addAuthZUsers(last)
                    runOnUiThread{
                        utils.showAd(interstitialAd)
                        finish()
                    }
                }.start()
            }
            addOnFailureListener { utils.showSnackBar(mainView, "Geofence Creation Failed") }
        }

    private fun validate(): Boolean {
        if(circle == null) {
            utils.showSnackBar(mainView, "Please long click on the map to create boundaries.")
            return false
        }
        if(title.text.toString().isEmpty()) {
            utils.showSnackBar(mainView, "Please enter a title.")
            return false
        }
        if(!(enter.isChecked || exit.isChecked || dwell.isChecked)) {
            utils.showSnackBar(mainView, "Please select at least one event.")
            return false
        }
        if(!utils.isLocationOn(this)) {
            utils.showSnackBar(mainView, "Please turn on device location.")
            return false;
        }
        return true
    }


    private fun createCircle(latLng: LatLng, rad: Double, zoom: Boolean = false) {
        circle?.remove()
        val ops = CircleOptions().apply {
            radius(rad)
            center(latLng)
            fillColor(GEOFENCE_COLOUR_NEW)
            strokeColor(GEOFENCE_COLOUR_NEW)
        }
        circle = mMap.addCircle(ops)
        if(zoom) mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
    }

    private fun prepUpdating(fence: Fence) {
        radius.isEnabled = false
        radius.setProgress(fence.radius.toInt(), true)
        enter.isChecked = fence.isEnter
        exit.isChecked = fence.isExit
        dwell.isChecked = fence.isDwell
        active.isChecked = fence.isActive
        safe.isChecked = fence.isSafe
        notify.isChecked = fence.isNotify
        hasDay(sunday, "sunday")
        hasDay(monday, "monday")
        hasDay(tuesday, "tuesday")
        hasDay(wednesday, "wednesday")
        hasDay(thursday, "thursday")
        hasDay(friday, "friday")
        hasDay(saturday, "saturday")
        from.setText(fence.from)
        to.setText(fence.to)
        title.text.clear()
        title.text.append(fence.title)
        description.text.clear()
        description.text.append(fence.description)
        createCircle(LatLng(fence.latitude, fence.longitude), fence.radius.toDouble(), true)
        btn.text = "Update"
        // disable immutable fields
        enter.isEnabled = false;
        exit.isEnabled = false;
        dwell.isEnabled = false;
    }

    private fun deleteFence() {
        locator.removeGeoFence(fence.key).addOnCompleteListener {
            if(!it.isSuccessful) return@addOnCompleteListener
            thread {

                repo.db.fenceDao().delete(fence.key)
                runOnUiThread {
                    utils.showSnackBar(mainView, "Geofence Deleted")
                    finish()
                }
            }
        }
    }

    private fun days() = arrayOf(
        check(sunday, "sunday"),
        check(monday, "monday"),
        check(tuesday, "tuesday"),
        check(wednesday, "wednesday"),
        check(thursday, "thursday"),
        check(friday, "friday"),
        check(saturday, "saturday")
    )
    .filter{ it.isNotBlank() }
    .joinToString(",")


    private fun check(box: CheckBox, value: String) = if(box.isChecked) value else ""

    private fun hasDay(box: CheckBox, value: String) {
        box.isChecked = fence.days.contains(value)
    }

    }
