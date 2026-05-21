@file:OptIn(ExperimentalWasmJsInterop::class)

package org.example.project.registration.repo

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import org.example.project.extensions.await
import org.example.project.registration.firebase.FirebaseApp
import org.example.project.registration.firebase.FirebaseAuth
import org.example.project.registration.firebase.createUserWithEmailAndPassword
import org.example.project.registration.firebase.getAuth
import org.example.project.registration.firebase.sendPasswordResetEmail
import org.example.project.registration.firebase.signInWithEmailAndPassword
import org.example.project.registration.firestore.doc
import org.example.project.registration.firestore.getFirestore
import org.example.project.registration.firestore.setDoc
import kotlinx.datetime.Clock
import org.example.project.extensions.JsVoid
import org.example.project.extensions.awaitSafe
import org.example.project.registration.firebase.createUserData
import org.example.project.registration.firebase.sendEmailVerification
import org.example.project.registration.firebase.signout

class WebAuthRepositoryImpl(
    private val app: FirebaseApp,
) : IAuthRepository {

    private val auth: FirebaseAuth = getAuth(app)

    @OptIn(ExperimentalWasmJsInterop::class)
    override fun signup(
        email: String,
        password: String,
        username: String,
        onResult: (Boolean, String?) -> Unit,
    ) {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val credential = createUserWithEmailAndPassword(auth, email, password).await()
                val uid = credential.user.uid

                try {
                    sendEmailVerification(credential.user).awaitSafe()
                } catch (e: Throwable) {
                }
                val db = getFirestore(app)
                val userDocRef = doc(db, "users/$uid")

                val userData = createUserData(
                    uid,
                    email,
                    username,
                    Clock.System.now().toString()
                )

                try {
                    setDoc(userDocRef, userData).await<JsVoid>()

                    signout(auth).awaitSafe()

                    onResult(true, uid)
                } catch (e: Throwable) {
                    onResult(false, uid)
                }

            } catch (e: Throwable) {
                onResult(false, e.message)
            }
        }
    }

    override fun login(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val credential = signInWithEmailAndPassword(auth, email, password).await()

                credential.user.reload().awaitSafe()

                if (credential.user.emailVerified) {
                    onResult(true, credential.user.uid)
                } else {
                    signout(auth).awaitSafe()
                    onResult(false, "EMAIL_NOT_VERIFIED")
                }

            } catch (e: Throwable) {
                onResult(false, e.message)
            }
        }
    }

    override fun sendEmailToResetPassword(
        email: String,
        onResult: (Boolean, String?) -> Unit,
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
                true
            }
            onResult(success, if (success) null else "Failed to send reset email")
        }
    }
}
