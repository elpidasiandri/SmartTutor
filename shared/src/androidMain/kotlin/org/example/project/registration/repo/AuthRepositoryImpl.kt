package org.example.project.registration.repo

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore

class AuthRepositoryImpl(
    private val auth: FirebaseAuth,
) : IAuthRepository {

    override fun signup(
        email: String,
        password: String,
        username: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    onResult(false, task.exception?.message)
                    return@addOnCompleteListener
                }

                val user = auth.currentUser
                val uid = user?.uid
                if (uid == null) {
                    onResult(false, "No user after create")
                    return@addOnCompleteListener
                }

                val profileUpdates = userProfileChangeRequest {
                    displayName = username
                }

                user.updateProfile(profileUpdates)
                    .addOnCompleteListener { profileTask ->
                        if (!profileTask.isSuccessful) {
                            Log.w("Signup", "Failed to set displayName: ${profileTask.exception}")
                        }

                        val data = hashMapOf(
                            "uid" to uid,
                            "email" to email,
                            "username" to username,
                            "createdAt" to com.google.firebase.Timestamp.now()
                        )
                        FirebaseFirestore.getInstance()
                            .collection("users")
                            .document(uid)
                            .set(data)
                            .addOnSuccessListener {
                                onResult(true, uid)
                            }
                            .addOnFailureListener { e ->
                                Log.e("Signup", "Failed to save user doc", e)
                                onResult(false, e.message)
                            }
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