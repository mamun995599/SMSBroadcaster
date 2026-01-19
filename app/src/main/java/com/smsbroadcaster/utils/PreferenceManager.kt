package com.smsbroadcaster.utils

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "sms_broadcaster_prefs"
        private const val KEY_SERVER_ENABLED = "server_enabled"
        private const val KEY_WEBSOCKET_PORT = "websocket_port"
        private const val KEY_HTTP_IP = "http_ip"
        private const val KEY_HTTP_PORT = "http_port"
        private const val KEY_HTTP_METHOD = "http_method"
        const val DEFAULT_WEBSOCKET_PORT = 8090
    }

    var isServerEnabled: Boolean
        get() = prefs.getBoolean(KEY_SERVER_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_SERVER_ENABLED, value).apply()

    var webSocketPort: Int
        get() = prefs.getInt(KEY_WEBSOCKET_PORT, DEFAULT_WEBSOCKET_PORT)
        set(value) = prefs.edit().putInt(KEY_WEBSOCKET_PORT, value).apply()

    var httpIp: String
        get() = prefs.getString(KEY_HTTP_IP, "") ?: ""
        set(value) = prefs.edit().putString(KEY_HTTP_IP, value).apply()

    var httpPort: Int
        get() = prefs.getInt(KEY_HTTP_PORT, 80)
        set(value) = prefs.edit().putInt(KEY_HTTP_PORT, value).apply()

    var httpMethod: String
        get() = prefs.getString(KEY_HTTP_METHOD, "GET") ?: "GET"
        set(value) = prefs.edit().putString(KEY_HTTP_METHOD, value).apply()
}