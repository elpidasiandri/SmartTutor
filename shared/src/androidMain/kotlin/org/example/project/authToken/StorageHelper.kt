package org.example.project.authToken

import android.content.Context
import androidx.core.content.edit

object StorageHelper {
    private const val PREF_NAME = "user_prefs"
    private const val KEY_TOKEN_EXPIRY = "token_expiry"

    fun saveTokenExpiry(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val expiryTime = System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000 // 30 days
        prefs.edit { putLong(KEY_TOKEN_EXPIRY, expiryTime) }
    }

    fun isTokenValid(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val expiryTime = prefs.getLong(KEY_TOKEN_EXPIRY, 0L)
        return System.currentTimeMillis() < expiryTime
    }

    fun clearToken(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit { clear() }
    }
}
