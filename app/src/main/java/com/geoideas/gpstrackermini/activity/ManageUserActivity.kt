package com.geoideas.gpstrackermini.activity

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.geoideas.gpstrackermini.R
import com.geoideas.gpstrackermini.activity.util.ActivityUtils
import com.geoideas.gpstrackermini.repository.Repository
import com.geoideas.gpstrackermini.repository.room.entity.User

import kotlinx.android.synthetic.main.activity_manage_user.*

class ManageUserActivity : AppCompatActivity() {

    private lateinit var username: EditText
    private lateinit var locationRequestBox: CheckBox
    private lateinit var number: EditText
    private lateinit var display: View
    private val utils = ActivityUtils()
    private var uid = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_user)
        setSupportActionBar(toolbar as Toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        username = findViewById(R.id.input_username)
        locationRequestBox = findViewById(R.id.box_location_request)
        number = findViewById(R.id.input_phone_number)
        display = findViewById(R.id.layout_create_user_info)
        val data = intent?.extras ?: null
        if(data != null){
            username.text.insert(0, data.getString("name"))
            locationRequestBox.isChecked = data.getBoolean("canRequestLocation")
            number.text.insert(0, data.getString("phoneNumber"))
            uid = data.getLong("id")
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_manage_user, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when(item?.itemId ?: -1){
            R.id.action_delete -> {
                deleteUser()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun updateUser(view: View) {
        val repo = Repository(this)
        val user = User().apply {
            name = username.text.toString()
            isRequestLocation = locationRequestBox.isChecked
            phoneNumber = number.text.toString()
            id = uid
        }
        if(validateAndShow(display, user)){
            val userDao = repo.db.userDao()
            Thread {
                userDao.update(user)
                runOnUiThread { utils.showSnackBar(display, "User updated") }
            }.start()
        }
    }

    private fun deleteUser() {
        val repo = Repository(this)
        val userDao = repo.db.userDao()
        val name = username.text.toString()
        Thread {
            val users = userDao.fetchUser(uid)
            if(users.isNotEmpty()) {
                userDao.delete(uid)
                runOnUiThread { startActivity(Intent(this, UserActivity::class.java)) }
            }
            else runOnUiThread { utils.showSnackBar(display,"Unable to delete user, username and code may not exist") }
        }.start()
    }

    private fun validateAndShow(view: View, user: User): Boolean{
        if(user.name.isBlank()) {
            utils.showSnackBar(view,"Name cannot be empty")
            return false
        }
        else if(user.name.length < 5) {
            utils.showSnackBar(view,"Name must be at least five characters")
            return false
        }
        else if(utils.invalidPhoneNumber(number)) {
            utils.showSnackBar(view, "invalid phone number")
            return false
        }
        return true
    }

}
