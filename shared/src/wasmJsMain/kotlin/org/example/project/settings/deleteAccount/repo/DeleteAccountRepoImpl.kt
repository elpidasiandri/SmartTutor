package org.example.project.settings.deleteAccount.repo

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.example.project.extensions.await
import org.example.project.registration.firebase.FirebaseApp
import org.example.project.registration.firebase.FirebaseAuth
import org.example.project.registration.firebase.getAuth

class DeleteAccountRepoImpl(private val app: FirebaseApp) : IDeleteAccountRepo {
    private val auth: FirebaseAuth = getAuth(app)
    override fun deleteAccount(onResult: (Boolean, String?) -> Unit) {
        CoroutineScope(Dispatchers.Default).launch {
            val user = auth.currentUser
            println("Q12345 ")
            if (user == null) {
                onResult(false, "User not logged in")
                println("Q12345 user null")
                return@launch
            }

            try {
                user.delete().await()
                println("Q12345 try")
                onResult(true, null)
            } catch (e: Throwable) {
                println("Q12345 catch ${e.message}")
                if (e.message?.contains("requires-recent-login") == true) {
                    onResult(false, "Please login again to delete your account")
                } else {
                    onResult(false, e.message)
                }
            }
        }
    }
}