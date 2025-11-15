package org.example.project.settings.changePassword.useCase

import org.example.project.settings.changePassword.repo.IChangePasswordRepo

class ChangePasswordUseCase(private val repo: IChangePasswordRepo) {
    operator fun invoke(
        oldPassword: String,
        newPassword: String,
        onResult: (Boolean, String?) -> Unit,
    ) =
        repo.changePassword(
            oldPassword = oldPassword,
            newPassword = newPassword,
            onResult = onResult
        )
}