package com.geoideas.gpstrackermini.coms

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.telephony.SmsMessage
import android.util.Log
import com.geoideas.gpstrackermini.location.Locator
import com.geoideas.gpstrackermini.repository.Repository
import com.geoideas.gpstrackermini.util.GoogleMapHelper
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import org.json.JSONObject

open class CommandExecutor(
    private val context: Context,
    private val locator: Locator,
    private val repo: Repository
) {

    private val TAG = CommandExecutor::class.java.simpleName
    private val UNKNOWN_USER = "UKNOW USER"

    private val P = Parser()
    private val userDao = repo.db.userDao()

    private val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    open fun handle(sms: SmsMessage) {
        val parser = Parser()

        val text = sms.messageBody.toLowerCase().trim()

        val locationRequestMessage = prefs.getString("sms_location_request_message","")
            .toLowerCase().trim()
        val locationRequest = text == locationRequestMessage

        val trustedMessage = prefs.getString("sms_location_request_trusted_message", "")
            .toLowerCase().trim()
        val trustedLocationRequest = text == trustedMessage

        if(locationRequest) {
            val handler = Handler().apply {
                success = { value ->
                    val message = if (locationRequest) {
                        val data = JSONObject(value.toString()).getJSONArray(parser.P_C)
                        GoogleMapHelper.locationURLText(data[1] as Double, data[0] as Double)
                    } else value.toString()
                    repo.smsSend(sms.originatingAddress, message)
                }
            }
            findLocation(sms.originatingAddress, true, handler)
        }
        else if(trustedLocationRequest) {
            val handler = Handler().apply {
                success = { value ->
                    val data = JSONObject(value.toString()).getJSONArray(parser.P_C)
                    val message = GoogleMapHelper.locationURLText(data[1] as Double, data[0] as Double)
                    repo.smsSend(sms.originatingAddress, message)
                }
            }
            findLocation(sms.originatingAddress, false, handler)
        }
        else {
            val command = parser.parser(sms.messageBody)
            if (command == parser.P_UNKNOWN) return
            val handler = Handler().apply {
                success = { value ->
                    val message = if (command == parser.P_L) {
                        val data = JSONObject(value.toString()).getJSONArray(parser.P_C)
                        GoogleMapHelper.locationURLText(data[1] as Double, data[0] as Double)
                    } else value.toString()
                    repo.smsSend(sms.originatingAddress, message)
                }
            }
            handle(command, sms.messageBody, handler)
        }
    }

    open fun handle(command: String, mes: String, handler: Handler){
        val json = JSONObject(mes)
        Log.d("CommandExecutor", json.toString(1))
        when(command){
            P.P_L ->  findLocation(json,handler)
        }
    }

    open fun findLocation(number: String, auth: Boolean = true, handler: Handler) =
        Thread {
            val users = repo.db.userDao().fetchUsers().filter { number.endsWith(it.phoneNumber) }
            if((!users.isNullOrEmpty() && users[0].isRequestLocation) || !auth)
                locator.currentLocation {
                    val res = JsonObject().apply {
                        val c = JsonArray()
                        c.add(it.longitude)
                        c.add(it.latitude)
                        add(P.P_C, c)
                        addProperty(P.P_A, it.accuracy)
                    }
                    handler.success(res)
                }
            else handler.failure(UNKNOWN_USER)
        }.start()

    open fun findLocation(json: JSONObject, handler: Handler) =
        Thread {
            if(authenticate(json))
                locator.currentLocation {
                    val res = JsonObject().apply {
                        val c = JsonArray()
                        c.add(it.longitude)
                        c.add(it.latitude)
                        add(P.P_C, c)
                        addProperty(P.P_A, it.accuracy)
                    }
                    handler.success(res)
                }
            else handler.failure(UNKNOWN_USER)
        }.start()

    open fun authenticate(json: JSONObject): Boolean {
        val users = userDao.fetchUser(json.getString(P.P_CODE))
        return users.isNotEmpty()
    }
}