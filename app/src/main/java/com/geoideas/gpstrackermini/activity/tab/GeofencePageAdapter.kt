package com.geoideas.gpstrackermini.activity.tab

import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.geoideas.gpstrackermini.activity.GeofenceListFragment
import com.geoideas.gpstrackermini.activity.GeofenceMapFragment

class GeofencePageAdapter(
    fm: FragmentManager,
    private val numOfTabs: Int
) : FragmentStatePagerAdapter(fm, numOfTabs) {

    override fun getItem(position: Int) = when(position) {
        0 -> GeofenceMapFragment()
        else -> GeofenceListFragment()
    }

    override fun getCount() = numOfTabs

}