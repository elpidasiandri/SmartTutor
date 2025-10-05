package org.example.project.registration.repo

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import org.example.project.extensions.await
import org.example.project.extensions.logD
import org.example.project.registration.firebase.FirebaseApp
import org.example.project.registration.firebase.FirebaseAuth
import org.example.project.registration.firebase.createUserWithEmailAndPassword
import org.example.project.registration.firebase.currentUser
import org.example.project.registration.firebase.getAuth
import org.example.project.registration.firebase.signInWithEmailAndPassword
import org.example.project.registration.firebase.signOut

class WebAuthRepositoryImpl(
    private val app: FirebaseApp,
) : IAuthRepository {

    private val auth: FirebaseAuth = getAuth(app)

    @OptIn(ExperimentalWasmJsInterop::class)
    override fun signup(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val credential = createUserWithEmailAndPassword(auth, email, password).await()
                logD("Q12345 Firebase signup initialized!")

                onResult(true, credential.user.uid)
            } catch (e: Throwable) {
               logD("Q12345 Firebase signup e ${e.message}!")
                onResult(false, null)
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
}