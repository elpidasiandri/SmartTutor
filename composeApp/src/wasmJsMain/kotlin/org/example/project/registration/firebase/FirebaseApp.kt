@file:JsModule("firebase/app")
@file:OptIn(ExperimentalWasmJsInterop::class)

package org.example.project.registration.firebase
import kotlin.js.*
external fun initializeApp(options: FirebaseOptions): FirebaseApp
@OptIn(ExperimentalWasmJsInterop::class)
external interface FirebaseApp : JsAny

@OptIn(ExperimentalWasmJsInterop::class)
@JsName("FirebaseOptions")
external interface FirebaseOptions: JsAny {
    var apiKey: String
    var authDomain: String
    var projectId: String
    var storageBucket: String
    var messagingSenderId: String
    var appId: String
}
