package com.geoideas.gpstrackermini.activity


import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.geoideas.gpstrackermini.R
import com.geoideas.gpstrackermini.activity.recycler.GeofenceRecycleAdapter
import com.geoideas.gpstrackermini.repository.Repository
import com.geoideas.gpstrackermini.repository.room.entity.Fence

/**
 * A simple [Fragment] subclass.
 */
class GeofenceListFragment : Fragment() {

    private val REFRESH_LIST = "~"
    private lateinit var recyclerView: RecyclerView
    private lateinit var search: EditText
    private lateinit var repo: Repository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_geofence_list, container, false)
        repo = Repository(GeofenceTabActivity.context)
        val searchButton = view.findViewById<Button>(R.id.btn_search)
        searchButton.setOnClickListener(::search)
        search = view.findViewById(R.id.input_search)
        onSearchTextChangeHandler()
        recyclerView = view.findViewById(R.id.geofences_list)
        return view
    }

    override fun onResume() {
        super.onResume()
        repo = Repository(GeofenceTabActivity.context)
        loadData()
    }

    fun onSearchTextChangeHandler() {
        search.addTextChangedListener( object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                if(search.text.toString().isEmpty())
                    loadData(REFRESH_LIST)
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        })
    }

    fun search(view: View) {
        loadData(search.text.toString())
    }

    private fun loadList(fences: List<Fence>, filter: String =  "") {
        val visibleFences = if(filter.isEmpty()) fences else filterFences(fences, filter)
        recyclerView.adapter = GeofenceRecycleAdapter(visibleFences, context!!, repo)
        recyclerView.layoutManager = LinearLayoutManager(context!!)
    }

    private fun filterFences(fences: List<Fence>, filter: String =  "") = fences.filter {
        filter == REFRESH_LIST ||
                it.title.toLowerCase().contains(filter.toLowerCase()) ||
                it.description.toLowerCase().contains(filter.toLowerCase())
    }

    private fun loadData(filter: String = "") {
        Thread {
            val fences = repo.db.fenceDao().fetchAll()
            GeofenceTabActivity.context.runOnUiThread {
                loadList(fences, filter)
            }
        }.start()
    }


}
