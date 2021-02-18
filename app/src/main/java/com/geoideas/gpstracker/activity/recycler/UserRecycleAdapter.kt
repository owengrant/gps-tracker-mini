package com.geoideas.gpstracker.activity.recycler

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.geoideas.gpstracker.R
import com.geoideas.gpstracker.activity.ManageUserActivity
import com.geoideas.gpstracker.repository.Repository
import com.geoideas.gpstracker.repository.room.entity.User
import kotlin.concurrent.thread

class UserRecycleAdapter(
    private val users: List<User>,
    private val context: Context,
    private val repo: Repository
): RecyclerView.Adapter<UserRecycleAdapter.UserViewHolder>() {

    private val inflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int) = UserViewHolder(inflater.inflate(R.layout.user_item, parent, false), this)

    override fun getItemCount() = users.size

    override fun onBindViewHolder(view: UserViewHolder, position: Int) = view.display(users[position])


    inner class UserViewHolder(
        private val view: View,
        private val adapter: UserRecycleAdapter
    ):  RecyclerView.ViewHolder(view), View.OnClickListener {

        override fun onClick(v: View?) {
            val user = users[layoutPosition]
            val intent = Intent(context, ManageUserActivity::class.java).apply {
                putExtra("name", user.name)
                putExtra("canRequestLocation", user.isRequestLocation)
                putExtra("phoneNumber", user.phoneNumber)
                putExtra("id", user.id)
            }
            context.startActivity(intent)
        }

        private val username = view.findViewById<TextView>(R.id.txt_username)
        private val phoneNumber = view.findViewById<TextView>(R.id.text_phone_number)
        private val locationRequest = view.findViewById<Switch>(R.id.switch_request_location)

        fun display(user: User) {
            username.setText(user.name)
            phoneNumber.setText(user.phoneNumber)
            locationRequest.isChecked = user.isRequestLocation
            view.setOnClickListener(this)
            locationRequest.setOnCheckedChangeListener { btn, checked ->
                thread {
                    user.isRequestLocation = checked
                    repo.db.userDao().update(user)
                }
            }
        }
    }

}