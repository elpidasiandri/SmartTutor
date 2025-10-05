package org.example.project.registration.firebase

@OptIn(ExperimentalWasmJsInterop::class)
private val firebaseConfig: FirebaseOptions = js("({})")

//external val process: dynamic

fun initFirebase(): FirebaseContext {
    firebaseConfig.apiKey = "AIzaSyCr9wncMGz_VtMXAMbRZ52VyIsAhXH1I04"
    firebaseConfig.authDomain = "smarttutor.firebaseapp.com"
    firebaseConfig.projectId = "smarttutor-ccddf"
    firebaseConfig.storage_bucket = "smarttutor-ccddf.firebasestorage.app"
    firebaseConfig.messagingSenderId = "464957724671"
    firebaseConfig.appId = "1:464957724671:android:3c4d2e9adabfdf80f679ca"

//    val apiKey = process.env.WEB_API_KEY as String

//    val firebaseConfig = js("""
//    ({
//        apiKey: WEB_API_KEY,
//        authDomain: WEB_AUTH_DOMAIN,
//        projectId: PROJECT_ID,
//        storageBucket: WEB_STORAGE_BUCKET,
//        messagingSenderId: SENDER_ID,
//        appId: APP_ID
//    })
//""")

    val app = initializeApp(firebaseConfig)
    val auth = getAuth(app)
    return FirebaseContext(app, auth)
}
