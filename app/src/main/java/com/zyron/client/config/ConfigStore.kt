package com.zyron.client.config

import android.content.Context
import org.json.JSONObject

class ConfigStore(context: Context) {
    private val prefs = context.getSharedPreferences("zyron", Context.MODE_PRIVATE)

    fun fpsCap(): Int = prefs.getInt("fpsCap", 60)
    fun setFpsCap(v: Int) = prefs.edit().putInt("fpsCap", v).apply()
    fun perfMode(): String = prefs.getString("perfMode", "balanced") ?: "balanced"
    fun setPerfMode(v: String) = prefs.edit().putString("perfMode", v).apply()
    fun json(): String = prefs.getString("configJson", "{}") ?: "{}"
    fun setJson(v: String) = prefs.edit().putString("configJson", v).apply()

    fun export(): String {
        val o = JSONObject()
        o.put("version", "1.0.0")
        o.put("fpsCap", fpsCap())
        o.put("perfMode", perfMode())
        o.put("config", JSONObject(json()))
        return o.toString(2)
    }
}
