@file:JsModule("firebase/app")

package org.example.project.registration.firebase
import kotlin.js.*
//external fun initializeApp(options: FirebaseOptions = definedExternally): FirebaseApp
external fun initializeApp(options: FirebaseOptions): FirebaseApp
external interface FirebaseApp : JsAny

external interface FirebaseOptions : JsAny {
    var apiKey: String
    var authDomain: String
    var projectId: String
}
//TODO ELPIDA

//fun initFirebase() {
//    val firebaseConfig = js("""
//        ({
//            apiKey: "AIzaSyXXX...",
//            authDomain: "your-project.firebaseapp.com",
//            projectId: "your-project-id",
//        })
//    """).unsafeCast<FirebaseOptions>()
//
//    val app = initializeApp(firebaseConfig)
//    getAuth(app)
//}
//external interface FirebaseApp
//external interface FirebaseOptions {
//    var apiKey: String
//    var authDomain: String
//    var projectId: String
//    // προσθέτεις ό,τι χρειάζεται
//}