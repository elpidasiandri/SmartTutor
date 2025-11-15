package org.example.project.registration.useCases.registration

import org.example.project.registration.useCases.registration.login.LoginUseCase
import org.example.project.registration.useCases.registration.resetPassword.SendEmailToResetPasswordUseCase
import org.example.project.registration.useCases.registration.signUp.SignUpUseCase

class RegistrationUseCase(
    private val signUpUseCase: SignUpUseCase,
    private val loginUseCase: LoginUseCase,
    private val sendEmailForChangePasswordUseCase: SendEmailToResetPasswordUseCase,
) {
    suspend fun logIn(
        email: String,
        password: String,
        onResult: (Boolean, String?) -> Unit,
    ) {
        loginUseCase(
            email = email,
            password = password,
            onResult = onResult
        )
    }
    fun sendEmailForChangePassword(
        email: String,
        onResult: (Boolean, String?) -> Unit,
    ) {
        sendEmailForChangePasswordUseCase(
            email = email,
            onResult = onResult
        )
    }

    suspend fun signUp(
        email: String,
        password: String,
        username: String,
        onResult: (Boolean, String?) -> Unit,
    ) {
        signUpUseCase(
            email = email,
            password = password,
            username = username,
            onResult = onResult
        )
    }
}