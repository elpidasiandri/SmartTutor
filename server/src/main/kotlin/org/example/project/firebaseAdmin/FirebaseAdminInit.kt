package org.example.project.firebaseAdmin

import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.firestore.Firestore
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.cloud.FirestoreClient
import java.io.ByteArrayInputStream

object FirebaseAdminInit {
    fun init() {

        if (FirebaseApp.getApps().isNotEmpty()) return

        val stream = try {
            val json = System.getenv("FIREBASE_SERVICE_ACCOUNT_JSON")
            if (json != null) {
                ByteArrayInputStream(json.toByteArray())
            } else {
                Thread.currentThread().contextClassLoader
                    .getResourceAsStream("firebase-service-account.json")
                    ?: error("firebase-service-account.json not found locally")
            }
        } catch (e: Exception) {
            throw IllegalStateException("Failed to load Firebase credentials", e)
        }

        val options = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(stream))
            .build()

        FirebaseApp.initializeApp(options)
    }
    fun firestore(): Firestore = FirestoreClient.getFirestore()

}