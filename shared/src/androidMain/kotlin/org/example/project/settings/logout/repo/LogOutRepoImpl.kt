package org.example.project.settings.logout.repo

import com.google.firebase.auth.FirebaseAuth

class LogOutRepoImpl(private val auth: FirebaseAuth) : ILogoutRepo {
    override fun logout(onResult: (Boolean, String?) -> Unit) {
        try {
            auth.signOut()
            onResult(true, null)
        } catch (e: Exception) {
            onResult(false, e.message)
        }
    }
}