@file:JsModule("firebase/auth")

package org.example.project.registration.firebase
import org.example.project.JsPromise
import org.example.project.JsVoid
import kotlin.js.*

external fun getAuth(app: FirebaseApp): FirebaseAuth

external interface FirebaseAuth : JsAny {
    fun createUserWithEmailAndPassword(email: String, password: String): JsPromise<UserCredential>
    fun signInWithEmailAndPassword(email: String, password: String): JsPromise<UserCredential>
    fun signOut(): JsPromise<JsVoid>
    val currentUser: FirebaseUser?
}

external interface UserCredential : JsAny {
    val user: FirebaseUser
}

external interface FirebaseUser : JsAny {
    val uid: String
    val email: String?
}
//TODO ELPIDA
//external fun getAuth(app: FirebaseApp = definedExternally): FirebaseAuth
//
//external interface FirebaseAuth  {
//    fun createUserWithEmailAndPassword(email: String, password: String): JsPromise<UserCredential>
//    fun signInWithEmailAndPassword(email: String, password: String): JsPromise<UserCredential>
//    fun signOut(): JsPromise<Unit>
//    val currentUser: FirebaseUser?
//}
//
//external interface UserCredential  {
//    val user: FirebaseUser
//}
//
//external interface FirebaseUser  {
//    val uid: String
//    val email: String?
//}
