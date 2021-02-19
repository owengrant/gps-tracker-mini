package com.geoideas.gpstrackermini.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.geoideas.gpstrackermini.R
import com.geoideas.gpstrackermini.activity.recycler.UserRecycleAdapter
import com.geoideas.gpstrackermini.repository.Repository

class UserActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: UserRecycleAdapter
    private lateinit var repo: Repository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)
        // setSupportActionBar(toolbar as Toolbar)
        // supportActionBar?.setDisplayHomeAsUpEnabled(true)
        repo = Repository(this)
        loadData()
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    fun createUserView(view: View){
        val intent = Intent(this, CreateUserActivity::class.java)
        startActivity(intent)
    }

    private fun loadData() {
        Thread {
            val userDao = repo.db.userDao()
            val users = userDao.fetchUsers()
            runOnUiThread {
                recyclerView = findViewById(R.id.users_list)
                adapter = UserRecycleAdapter(users, this, repo)
                recyclerView.adapter = adapter
                recyclerView.layoutManager = LinearLayoutManager(this)
            }
        }.start()
    }

}
