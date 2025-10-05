@file:JsModule("firebase/app")

package org.example.project.registration.firebase
import kotlin.js.*
external fun initializeApp(options: FirebaseOptions): FirebaseApp
external interface FirebaseApp : JsAny

external interface FirebaseOptions : JsAny {
    var apiKey: String
    var authDomain: String
    var messagingSenderId: String
    var projectId: String
    var appId: String
    var storage_bucket: String
}