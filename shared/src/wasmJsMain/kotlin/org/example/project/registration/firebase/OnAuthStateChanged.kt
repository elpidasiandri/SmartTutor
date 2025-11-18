@file:JsModule("firebase/auth")
@file:OptIn(ExperimentalWasmJsInterop::class)

package org.example.project.registration.firebase

external fun onAuthStateChanged(
    auth: FirebaseAuth,
    callback: (FirebaseUser?) -> Unit
)
