package com.geoideas.gpstracker.activity

import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.geoideas.gpstracker.R
import com.geoideas.gpstracker.activity.util.ActivityUtils
import com.geoideas.gpstracker.repository.Repository
import com.geoideas.gpstracker.repository.room.entity.User
import android.app.Activity
import android.database.Cursor
import android.util.Log


class CreateUserActivity : AppCompatActivity() {

    private val RESULT_PICK_CONTACT = 0
    private lateinit var username: EditText
    private lateinit var locationRequestBox: CheckBox
    private lateinit var number: EditText
    private lateinit var display: View
    private val utils = ActivityUtils()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_user)
        username = findViewById(R.id.input_username)
        locationRequestBox = findViewById(R.id.box_location_request)
        number = findViewById(R.id.input_phone_number)
        display = findViewById(R.id.layout_create_user_info)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_create_user, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return if(item?.itemId == R.id.action_select_contact) {
            val intent = Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
            intent.type = ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE
            startActivityForResult(intent, RESULT_PICK_CONTACT)
            true
        } else super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                RESULT_PICK_CONTACT -> {
                    var cursor: Cursor? = null
                    try {
                        var phoneNo: String? = null
                        var name: String? = null
                        val uri = data!!.data
                        cursor = contentResolver.query(uri!!, null, null, null, null)
                        cursor!!.moveToFirst()
                        val phoneIndex = cursor!!.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                        val nameIndex = cursor!!.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                        phoneNo = cursor?.getString(phoneIndex)
                        name = cursor?.getString(nameIndex)
                        username.text.clear()
                        username.text.insert(0, name)
                        number.text.clear()
                        number.text.insert(0, phoneNo)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        } else {
            Log.d("CreateUser", "Not able to pick contact")
        }
    }

    private fun createFail() = utils.showSnackBar(display,"User is similar code or authentication code exists")

    private fun createSuccessful() {
        username.text.clear()
        locationRequestBox.isChecked = true
        number.text.clear()
        utils.showSnackBar(display, "User created")
    }

    fun createUser(view: View){
        val repo = Repository(this)
        val user = User().apply {
            name = username.text.toString()
            phoneNumber = number.text.toString()
            isRequestLocation = locationRequestBox.isChecked
        }
        if(validateAndShow(display, user)){
            val userDao = repo.db.userDao()
            Thread {
                val users = userDao.fetchUser(user.code)
                if(users.isEmpty()) {
                    userDao.insert(user)
                    runOnUiThread { createSuccessful() }
                }
                else runOnUiThread { createFail() }

            }.start()
        }
    }

    private fun validateAndShow(view: View, user: User): Boolean{
        if(user.name.isBlank()) {
            utils.showSnackBar(view,"Name cannot be empty")
            return false
        }
        else if(utils.invalidPhoneNumber(number)) {
            utils.showSnackBar(view, "invalid phone number")
            return false
        }
        return true
    }
}
