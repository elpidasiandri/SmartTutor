package org.example.project.registration.useCases.registration.resetPassword

import org.example.project.registration.repo.IAuthRepository

class UpdatePasswordUseCase(
    val repo: IAuthRepository
) {
    suspend operator fun invoke(
        email: String,
        newPassword: String,
        onResult: (Boolean, String?) -> Unit,
    ) = repo.updatePassword(
        newPassword = newPassword,
        onResult = onResult
    )
}
