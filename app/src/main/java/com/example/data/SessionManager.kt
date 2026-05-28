package com.example.data

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("app_session", Context.MODE_PRIVATE)

    fun loginUser(phoneNumber: String, role: String) {
        prefs.edit().apply {
            putString("PHONE_NUMBER", phoneNumber)
            putString("ROLE", role)
            putBoolean("IS_LOGGED_IN", true)
            apply()
        }
    }

    fun logout() {
        prefs.edit().clear().apply()
    }

    fun isLoggedIn(): Boolean = prefs.getBoolean("IS_LOGGED_IN", false)

    fun getCurrentPhone(): String? = prefs.getString("PHONE_NUMBER", null)
    
    fun getCurrentRole(): String? = prefs.getString("ROLE", "STUDENT")
}
