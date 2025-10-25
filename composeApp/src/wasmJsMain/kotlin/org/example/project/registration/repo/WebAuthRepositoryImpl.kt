@file:OptIn(ExperimentalWasmJsInterop::class)
package org.example.project.registration.repo

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import org.example.project.extensions.await
import org.example.project.extensions.logD
import org.example.project.registration.firebase.FirebaseApp
import org.example.project.registration.firebase.FirebaseAuth
import org.example.project.registration.firebase.changePassword
import org.example.project.registration.firebase.createUserWithEmailAndPassword
import org.example.project.registration.firebase.currentUser
import org.example.project.registration.firebase.getAuth
import org.example.project.registration.firebase.sendPasswordResetEmail
import org.example.project.registration.firebase.signInWithEmailAndPassword
import org.example.project.registration.firebase.signOut
import org.example.project.registration.firestore.doc
import org.example.project.registration.firestore.getFirestore
import org.example.project.registration.firestore.setDoc
import kotlinx.datetime.Clock
import org.example.project.extensions.JsVoid
import org.example.project.registration.firebase.createUserData

class WebAuthRepositoryImpl(
    private val app: FirebaseApp,
) : IAuthRepository {

    private val auth: FirebaseAuth = getAuth(app)
    @OptIn(ExperimentalWasmJsInterop::class)
    override fun signup(
        email: String,
        password: String,
        username: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                logD("Q12345 Before createUserWithEmailAndPassword")
                val credential = createUserWithEmailAndPassword(auth, email, password).await()
                val uid = credential.user.uid
                logD("Q12345 uid = $uid")
                logD("Q12345 Clock = ${Clock.System.now().toString()}")

                val db = getFirestore(app)
                val userDocRef = doc(db, "users/$uid")

                val userData = createUserData(
                    uid,
                    email,
                    username,
                    Clock.System.now().toString()
                )
                logD("Q12345 Before setDoc")
                if (userDocRef == null) {
                    logD("userDocRef is null!")
                }
                try {
                    logD("Q12345 try")
                    setDoc(userDocRef, userData).await<JsVoid>()
                    logD("Q12345 setDoc success")
                    onResult(true, uid)
                } catch (e: Throwable) {
                    logD("setDoc failed: ${e.message}")
                    onResult(false, e.message)
                }

            } catch (e: Throwable) {
                logD("Q12345 createUser failed: ${e.message}")
                onResult(false, e.message)
            }
        }
    }

    override fun login(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        CoroutineScope(Dispatchers.Default).launch() {
            try {

                val credential = signInWithEmailAndPassword(auth,email, password).await()
                logD("Q12345 Firebase login success")

                onResult(true, credential.user.uid)
            } catch (e: Throwable) {
                logD("Q12345 Firebase login e ${e.message} ${e.cause}!")

                onResult(false, null)
            }
        }
    }

    @OptIn(ExperimentalWasmJsInterop::class)
    override fun logout() {
        CoroutineScope(Dispatchers.Default).launch {
            //TODO
            try {
                signOut(auth).await()
            } catch (_: Throwable) {
                // handle error αν θες
            }
        }
    }

    override fun currentUserId(): String? = currentUser()?.uid
    override fun sendEmailToResetPassword(
        email: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        if (email.isBlank()) {
            onResult(false, "Email is empty")
            return
        }

        CoroutineScope(Dispatchers.Default).launch {
            val success = try {
                sendPasswordResetEmail(auth, email).await()
                true
            } catch (e: Throwable) {
                logD("Q12345 Firebase reset email error ignored: ${e.message}")
                true
            }
            onResult(success, if (success) null else "Failed to send reset email")
        }
    }

    override fun updatePassword(
        newPassword: String,
        onResult: (Boolean, String?) -> Unit,
    ) {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val user = currentUser()
                if (user == null) {
                    logD("Q12345 updatePassword user = null")
                    onResult(false, "No logged-in user")
                    return@launch
                }

                changePassword(user, newPassword).await()
                logD("Q12345 Firebase password updated!")
                onResult(true, null)
            } catch (e: Throwable) {
                logD("Q12345 Firebase update password error ${e.message}")
                onResult(false, e.message)
            }
        }
    }
}
