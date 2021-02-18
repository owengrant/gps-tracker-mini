package com.geoideas.gpstracker.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.google.android.material.tabs.TabLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.geoideas.gpstracker.R
import com.geoideas.gpstracker.activity.tab.GeofencePageAdapter
import com.geoideas.gpstracker.activity.util.ActivityUtils
import com.geoideas.gpstracker.repository.Repository
import com.geoideas.gpstracker.util.AppConstant
import kotlin.concurrent.thread

class GeofenceTabActivity : AppCompatActivity() {

    val TAG = GeofenceTabActivity::class.java.simpleName

    private lateinit var viewPager: ViewPager
    private lateinit var repo: Repository

    companion object {
        lateinit var viewPager: ViewPager
        lateinit var context: AppCompatActivity
        val MAX_GEOFENCE_COUNT = -1
        val GEOFENCE_FILLED_ERROR = "Only $MAX_GEOFENCE_COUNT geofences can be created with this version of Geo SMS."
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_geofence_tab)

        val tabLayout = findViewById<TabLayout>(R.id.tabs)
        tabLayout.addTab(tabLayout.newTab().setText("Map"))
        tabLayout.addTab(tabLayout.newTab().setText("List"))
        tabLayout.tabGravity = TabLayout.GRAVITY_FILL;
        viewPager = findViewById<ViewPager>(R.id.view_pager)
        viewPager.adapter = GeofencePageAdapter(supportFragmentManager, tabLayout.tabCount)

        viewPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))
        tabLayout.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(viewPager))
        viewPager = viewPager
        context = this

        repo = Repository(this)
    }

    fun create(view: View) {
        thread {
            val count = repo.db.fenceDao().count()
            runOnUiThread {
                if(count >= MAX_GEOFENCE_COUNT && !AppConstant.ISPRO)
                    ActivityUtils().showPurchaseDialog(this, GEOFENCE_FILLED_ERROR)
                else {
                    val intent = Intent(this, CreateGeofenceActivity::class.java)
                    startActivity(intent)
                }
            }
        }
    }

    fun back(view: View) {
        finish()
    }
}