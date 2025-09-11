package org.example.project.registration.repo

import com.google.firebase.auth.FirebaseAuth

class AuthRepositoryImpl(
    private val auth: FirebaseAuth
) : IAuthRepository {

    override fun signup(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, auth.currentUser?.uid)
                } else {
                    onResult(false, null)
                }
            }
    }

    override fun login(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, auth.currentUser?.uid)
                } else {
                    onResult(false, null)
                }
            }
    }

    override fun logout() {
        auth.signOut()
    }

    override fun currentUserId(): String? = auth.currentUser?.uid
}