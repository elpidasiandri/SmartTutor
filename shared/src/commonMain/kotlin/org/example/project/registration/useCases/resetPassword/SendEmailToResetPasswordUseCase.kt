package org.example.project.registration.useCases.registration.resetPassword

import org.example.project.registration.repo.IAuthRepository

class SendEmailToResetPasswordUseCase(
    val repo: IAuthRepository,
) {
    operator fun invoke(
        email: String,
        onResult: (Boolean, String?) -> Unit,
    ) = repo.sendEmailToResetPassword(
        email = email,
        onResult = onResult
    )
}
