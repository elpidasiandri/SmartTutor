package org.example.project.registration.useCases.registration

import org.example.project.registration.useCases.registration.login.LoginUseCase
import org.example.project.registration.useCases.registration.logOut.LogoutUseCase
import org.example.project.registration.useCases.registration.resetPassword.UpdatePasswordUseCase
import org.example.project.registration.useCases.registration.resetPassword.SendEmailToResetPasswordUseCase
import org.example.project.registration.useCases.registration.signUp.SignUpUseCase

class RegistrationUseCase(
    private val signUpUseCase: SignUpUseCase,
    private val loginUseCase: LoginUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val updatePasswordUseCase: UpdatePasswordUseCase,
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

    //TODO FOR UPDATE PASSWORD
    suspend fun updatePassword(
        email: String,
        newPassword: String,
        onResult: (Boolean, String?) -> Unit,
    ) {
        updatePasswordUseCase(
            email = email,
            newPassword = newPassword,
            onResult = onResult
        )
    }

    suspend fun sendEmailForChangePassword(
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

    suspend fun logout(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        //gia android
        //FirebaseAuth.getInstance().signOut()
        //StorageHelper.clearToken(context)

        //gia web
        //  WebStorageHelper.clearToken()
        //    FirebaseAuth.getInstance().signOut()
        logoutUseCase(email, password, onResult = onResult)
    }
}