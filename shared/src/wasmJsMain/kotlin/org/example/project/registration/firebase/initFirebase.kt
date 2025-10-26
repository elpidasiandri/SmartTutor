package org.example.project.registration.firebase

import org.example.project.shared.FirebaseConfig

@OptIn(ExperimentalWasmJsInterop::class)
private val firebaseConfig: FirebaseOptions = js("({})")

fun initFirebase(): FirebaseContext {
    firebaseConfig.apiKey = FirebaseConfig.WEB_API_KEY
    firebaseConfig.authDomain = FirebaseConfig.WEB_AUTH_DOMAIN
    firebaseConfig.projectId = FirebaseConfig.PROJECT_ID
    firebaseConfig.storageBucket = FirebaseConfig.WEB_STORAGE_BUCKET
    firebaseConfig.messagingSenderId = FirebaseConfig.SENDER_ID
    firebaseConfig.appId = FirebaseConfig.APP_ID

    val app = initializeApp(firebaseConfig)
    val auth = getAuth(app)
    return FirebaseContext(app, auth)
}
