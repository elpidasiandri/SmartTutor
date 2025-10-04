package org.example.project.registration.repo

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.example.project.await
import org.example.project.registration.firebase.FirebaseApp
import org.example.project.registration.firebase.FirebaseAuth
import org.example.project.registration.firebase.getAuth
//TODO ELPIDA
class WebAuthRepositoryImpl(
    private val app: FirebaseApp,
) : IAuthRepository {

    private val auth: FirebaseAuth = getAuth(app)

    override fun signup(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val credential = auth.createUserWithEmailAndPassword(email, password).await()
                onResult(true, credential.user.uid)
            } catch (e: Throwable) {
                onResult(false, null)
            }
        }
    }

    override fun login(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        CoroutineScope(Dispatchers.Default).launch() {
            try {
                val credential = auth.signInWithEmailAndPassword(email, password).await()
                onResult(true, credential.user.uid)
            } catch (e: Throwable) {
                onResult(false, null)
            }
        }
    }

    override fun logout() {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                auth.signOut().await()
            } catch (_: Throwable) {
                // handle error αν θες
            }
        }
    }

    override fun currentUserId(): String? = auth.currentUser?.uid
}