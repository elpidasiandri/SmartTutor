package org.example.project.registration.repo

interface IAuthRepository {
    fun signup(email: String, password: String, onResult: (Boolean, String?) -> Unit)
    fun login(email: String, password: String, onResult: (Boolean, String?) -> Unit)
    fun logout()
    fun currentUserId(): String?
}