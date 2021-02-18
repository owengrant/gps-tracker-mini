package com.geoideas.gpstracker.coms

import com.google.gson.Gson
import com.google.gson.JsonObject
import org.json.JSONObject

object Command {
    private val P = Parser()

    fun currentLocation(code: String) = JsonObject().run {
        addProperty(P.P_OP, P.P_L)
        addProperty(P.P_CODE, code)
        Gson().toJson(this)
    }

    fun tracks(code: String, start: String, end: String) = JSONObject().run {
        put(P.P_OP, P.P_TB)
        put(P.P_CODE, code)
        put(P.P_S, start)
        put(P.P_E, end)
        toString()
    }

    fun tracksAccuracy(code: String, start: String, end: String, acc: Int) = JSONObject().run {
        put(P.P_OP, P.P_TBA)
        put(P.P_CODE, code)
        put(P.P_S, start)
        put(P.P_E, end)
        put(P.P_A, acc)
        toString()
    }

    fun tracksGradient(code: String, start: String, end: String, max: Int, gradient: Boolean) = JSONObject().run {
        put(P.P_OP, P.P_TB)
        put(P.P_CODE, code)
        put(P.P_S, start)
        put(P.P_E, end)
        put("max", max)
        put("gradient", gradient)
        toString()
    }
}