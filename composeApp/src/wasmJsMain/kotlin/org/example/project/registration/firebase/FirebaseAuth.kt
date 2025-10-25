@file:JsModule("firebase/auth")
package org.example.project.registration.firebase

import org.example.project.extensions.JsPromise
import org.example.project.extensions.JsVoid
import kotlin.js.*

external fun getAuth(app: FirebaseApp): FirebaseAuth

@OptIn(ExperimentalWasmJsInterop::class)
external interface FirebaseAuth : JsAny

external fun signInWithEmailAndPassword(
    auth: FirebaseAuth,
    email: String,
    password: String,
): JsPromise<UserCredential>

external fun createUserWithEmailAndPassword(
    auth: FirebaseAuth,
    email: String,
    password: String,
): JsPromise<UserCredential>

@OptIn(ExperimentalWasmJsInterop::class)
external fun signOut(auth: FirebaseAuth): Promise<JsVoid>

@OptIn(ExperimentalWasmJsInterop::class)
external fun currentUser(): FirebaseUser?

@OptIn(ExperimentalWasmJsInterop::class)
external interface UserCredential : JsAny {
    val user: FirebaseUser
}

@OptIn(ExperimentalWasmJsInterop::class)
external interface FirebaseUser : JsAny {
    val uid: String
    val email: String?
}

@JsName("sendPasswordResetEmail")
external fun sendPasswordResetEmail(
    auth: FirebaseAuth,
    email: String,
): JsPromise<JsVoid>

@JsName("updatePassword")
external fun changePassword(
    user: FirebaseUser,
    newPassword: String,
): JsPromise<JsVoid>
