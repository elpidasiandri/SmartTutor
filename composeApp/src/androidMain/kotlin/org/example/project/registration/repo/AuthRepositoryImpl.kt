package org.example.project.registration.repo

import com.google.firebase.auth.FirebaseAuth

class AuthRepositoryImpl(
    private val auth: FirebaseAuth,
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

    override fun sendEmailToResetPassword(email: String, onResult: (Boolean, String?) -> Unit) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, null)
                } else {
                    onResult(false, task.exception?.message)
                }
            }
    }

    override fun updatePassword(newPassword: String, onResult: (Boolean, String?) -> Unit) {
        val user = auth.currentUser
        println("Q12345 changePassword  user =  ${user}")

        if (user == null) {
            onResult(false, "No logged-in user")
            return
        }

        user.updatePassword(newPassword)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, null)
                } else {
                    onResult(false, task.exception?.message)
                }
            }
    }
}