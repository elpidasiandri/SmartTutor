@file:JsModule("firebase/firestore")
@file:OptIn(ExperimentalWasmJsInterop::class)
package org.example.project.registration.firestore

import org.example.project.extensions.JsVoid
import org.example.project.registration.firebase.FirebaseApp
import kotlin.js.*

@OptIn(ExperimentalWasmJsInterop::class)
external interface Firestore : JsAny

@OptIn(ExperimentalWasmJsInterop::class)
external interface DocumentReference : JsAny

@OptIn(ExperimentalWasmJsInterop::class)
external interface CollectionReference : JsAny

@OptIn(ExperimentalWasmJsInterop::class)
external fun getFirestore(app: FirebaseApp): Firestore

@OptIn(ExperimentalWasmJsInterop::class)
external fun collection(firestore: Firestore, path: String): CollectionReference

@OptIn(ExperimentalWasmJsInterop::class)
external fun doc(firestore: Firestore, path: String): DocumentReference

@OptIn(ExperimentalWasmJsInterop::class)
external fun setDoc(ref: DocumentReference, data: UserData): Promise<JsVoid>