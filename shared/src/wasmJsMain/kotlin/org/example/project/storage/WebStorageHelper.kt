package org.example.project.storage

import kotlinx.browser.localStorage
import kotlinx.datetime.Clock

object WebStorageHelper {
    private const val TOKEN_EXPIRY_KEY = "token_expiry"

    fun saveTokenExpiry() {
        val expiryTime = Clock.System.now().toEpochMilliseconds() + 30L * 24 * 60 * 60 * 1000 // 30 days
        localStorage.setItem(TOKEN_EXPIRY_KEY, expiryTime.toString())
    }

    fun isTokenValid(): Boolean {
        val expiryTime = localStorage.getItem(TOKEN_EXPIRY_KEY)?.toLongOrNull() ?: 0L
        return Clock.System.now().toEpochMilliseconds() < expiryTime
    }

    fun clearToken() {
        localStorage.removeItem(TOKEN_EXPIRY_KEY)
    }
}
