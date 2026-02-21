package org.example.project.firebaseAdmin

import com.google.firebase.auth.FirebaseAuth
import io.ktor.http.HttpHeaders
import io.ktor.server.application.ApplicationCall

fun ApplicationCall.requireUidDevOk(): String {
    val devUid = request.headers["X-Dev-UserId"]?.trim()
    if (!devUid.isNullOrBlank()) return devUid

    val authHeader = request.headers[HttpHeaders.Authorization]
        ?: throw IllegalArgumentException("Missing Authorization header")

    if (!authHeader.startsWith("Bearer ", ignoreCase = true)) {
        throw IllegalArgumentException("Invalid Authorization header (expected Bearer token)")
    }

    val token = authHeader.removePrefix("Bearer").trim()
    if (token.isBlank()) throw IllegalArgumentException("Missing Bearer token")

    val decoded = FirebaseAuth.getInstance().verifyIdToken(token)
    return decoded.uid
}