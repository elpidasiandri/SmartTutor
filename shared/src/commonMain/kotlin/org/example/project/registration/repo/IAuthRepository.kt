package org.example.project.registration.repo

interface IAuthRepository {
    fun signup(
        email: String,
        password: String,
        username: String,
        onResult: (Boolean, String?) -> Unit,
    )
    fun login(email: String, password: String, onResult: (Boolean, String?) -> Unit)
    fun logout()
    fun currentUserId(): String?
    fun sendEmailToResetPassword(email: String, onResult: (Boolean, String?) -> Unit)
    fun updatePassword(newPassword: String, onResult: (Boolean, String?) -> Unit)
}