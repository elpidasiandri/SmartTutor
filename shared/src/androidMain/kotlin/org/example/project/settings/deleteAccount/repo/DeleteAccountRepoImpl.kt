package org.example.project.settings.deleteAccount.repo

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DeleteAccountRepoImpl(private val auth: FirebaseAuth) : IDeleteAccountRepo {
    override fun deleteAccount(onResult: (Boolean, String?) -> Unit) {
        val user = auth.currentUser
        if (user == null) {
            onResult(false, "No logged-in user")
            return
        }

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(user.uid)
            .delete()
            .addOnCompleteListener { docTask ->
                if (!docTask.isSuccessful) {
                    onResult(false, docTask.exception?.message)
                }

                user.delete()
                    .addOnCompleteListener { deleteTask ->
                        if (deleteTask.isSuccessful) {
                            onResult(true, null)
                        } else {
                            onResult(false, deleteTask.exception?.message)
                        }
                    }
            }
    }

}