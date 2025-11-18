@file:JsModule("firebase/auth")
@file:OptIn(ExperimentalWasmJsInterop::class)
package org.example.project.registration.firebase

external object EmailAuthProvider {
    @JsName("credential")
    fun credential(email: String, password: String): AuthCredential
}

@OptIn(ExperimentalWasmJsInterop::class)
external interface AuthCredential : JsAny
