package org.example.project.settings.changePassword.repo

interface IChangePasswordRepo {
    fun changePassword(
        oldPassword: String,
        newPassword: String,
        onResult: (Boolean, String?) -> Unit,
    )
}