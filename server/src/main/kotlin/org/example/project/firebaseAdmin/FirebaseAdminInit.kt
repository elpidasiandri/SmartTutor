package org.example.project.firebaseAdmin

import com.google.auth.oauth2.GoogleCredentials
import com.google.auth.oauth2.ServiceAccountCredentials
import com.google.cloud.firestore.Firestore
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.cloud.FirestoreClient
import java.io.ByteArrayInputStream

object FirebaseAdminInit {

    private lateinit var credentials: GoogleCredentials
    private lateinit var projectId: String

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
            throw IllegalStateException(
                "Failed to load Firebase credentials",
                e
            )
        }

        credentials = GoogleCredentials.fromStream(stream)

        projectId = (credentials as? ServiceAccountCredentials)
            ?.projectId
            ?.takeIf { it.isNotBlank() }
            ?: error(
                "Could not determine Firebase projectId from service account"
            )

        val options = FirebaseOptions.builder()
            .setCredentials(credentials)
            .setProjectId(projectId)
            .build()

        FirebaseApp.initializeApp(options)
    }

    fun firestore(): Firestore =
        FirestoreClient.getFirestore()

    fun credentials(): GoogleCredentials =
        credentials

    fun projectId(): String =
        projectId
}
