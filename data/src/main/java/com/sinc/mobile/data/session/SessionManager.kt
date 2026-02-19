package com.sinc.mobile.data.session

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class SessionManager @Inject constructor(@ApplicationContext context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveAuthToken(token: String) {
        val editor = prefs.edit()
        editor.putString(KEY_AUTH_TOKEN, token)
        editor.apply()
    }

    fun getAuthToken(): String? {
        return prefs.getString(KEY_AUTH_TOKEN, null)
    }

    fun clearAuthToken() {
        val editor = prefs.edit()
        editor.remove(KEY_AUTH_TOKEN)
        editor.apply()
    }

    fun saveLogisticsConfig(isOpen: Boolean, nextVisit: String?, deadline: String?) {
        val editor = prefs.edit()
        editor.putBoolean(KEY_LOGISTICS_OPEN, isOpen)
        editor.putString(KEY_NEXT_VISIT, nextVisit)
        editor.putString(KEY_ORDER_DEADLINE, deadline)
        editor.apply()
    }

    fun isLogisticsOpen(): Boolean {
        return prefs.getBoolean(KEY_LOGISTICS_OPEN, true)
    }

    fun getNextVisitDate(): String? {
        return prefs.getString(KEY_NEXT_VISIT, null)
    }

    fun getOrderDeadline(): String? {
        return prefs.getString(KEY_ORDER_DEADLINE, null)
    }

    companion object {
        private const val PREFS_NAME = "sinc_prefs"
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_LOGISTICS_OPEN = "logistics_open"
        private const val KEY_NEXT_VISIT = "next_visit_date"
        private const val KEY_ORDER_DEADLINE = "order_deadline"
    }
}