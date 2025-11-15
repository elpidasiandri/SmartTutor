package org.example.project.settings.changePassword.repo

import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

class ChangePasswordRepoImpl(
    private val auth: FirebaseAuth,
) : IChangePasswordRepo {

    override fun changePassword(
        oldPassword: String,
        newPassword: String,
        onResult: (Boolean, String?) -> Unit,
    ) {
        val user = auth.currentUser

        if (user == null || user.email.isNullOrEmpty()) {
            onResult(false, "No logged-in user")
            return
        }

        val credential = EmailAuthProvider.getCredential(user.email!!, oldPassword)

        user.reauthenticate(credential)
            .addOnCompleteListener { reauthTask ->
                if (reauthTask.isSuccessful) {
                    user.updatePassword(newPassword)
                        .addOnCompleteListener { updateTask ->
                            if (updateTask.isSuccessful) {
                                onResult(true, null)
                            } else {
                                onResult(false, updateTask.exception?.message)
                            }
                        }
                } else {
                    onResult(false, "Old password is incorrect")
                }
            }
    }
}