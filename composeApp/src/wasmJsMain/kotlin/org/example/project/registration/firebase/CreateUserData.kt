@file:JsModule("./firebaseWrapper.js")
@file:OptIn(ExperimentalWasmJsInterop::class)

package org.example.project.registration.firebase

import org.example.project.registration.firestore.UserData
import kotlin.js.JsModule

@OptIn(ExperimentalWasmJsInterop::class)
external fun createUserData(
    uid: String,
    email: String,
    username: String,
    createdAt: String,
): UserData