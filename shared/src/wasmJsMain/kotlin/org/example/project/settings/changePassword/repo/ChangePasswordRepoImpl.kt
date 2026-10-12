package org.example.project.settings.changePassword.repo

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.example.project.extensions.await
import org.example.project.extensions.awaitSafe
import org.example.project.registration.AuthUserHolder
import org.example.project.registration.firebase.EmailAuthProvider
import org.example.project.registration.firebase.FirebaseApp
import org.example.project.registration.firebase.FirebaseAuth
import org.example.project.registration.firebase.changePassword
import org.example.project.registration.firebase.getAuth
import org.example.project.registration.firebase.reauthenticateWithCredential

class ChangePasswordRepoImpl(private val app: FirebaseApp) : IChangePasswordRepo {
    private val auth: FirebaseAuth = getAuth(app)
    override fun changePassword(
        oldPassword: String,
        newPassword: String,
        onResult: (Boolean, String?) -> Unit,
    ) {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                var user = AuthUserHolder.currentUser

                if (user == null) {
                    user = auth.currentUser
                    if (user == null) return@launch
                }

                val email = user.email
                if (email.isNullOrBlank()) {
                    onResult(false, "Missing email")
                    return@launch
                }

                val credential = EmailAuthProvider.credential(email, oldPassword)

                try {
                    reauthenticateWithCredential(user, credential).await()
                } catch (e: Throwable) {
                    val message = when {
                        e.message?.contains("auth/wrong-password") == true ||
                                e.message?.contains("auth/invalid-credential") == true ->
                            "Old password is incorrect"

                        e.message?.contains("auth/too-many-requests") == true ->
                            "Too many attempts. Please try again later."

                        else ->
                            e.message ?: "Reauthentication failed"
                    }

                    onResult(false, message)
                    return@launch
                }

                val okPass = changePassword(user, newPassword).awaitSafe()
                if (!okPass) {
                    onResult(false, "Password update failed")
                    return@launch
                }
                AuthUserHolder.currentUser = user
                onResult(true, null)

            } catch (e: Throwable) {
                onResult(false, e.message)
            }
        }
    }
}
