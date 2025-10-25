@file:Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")

package org.example.project

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import org.example.project.registration.RegistrationComposable
import org.example.project.registration.firebase.initFirebase
import org.example.project.registration.repo.WebAuthRepositoryImpl
import org.example.project.registration.viewModel.WebRegistrationController

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val firebaseContext = initFirebase()
    val authRepo = WebAuthRepositoryImpl(firebaseContext.app)
    val controller = WebRegistrationController(authRepo)

    ComposeViewport("tutorApp") {
        val state by controller.state.collectAsState()

        RegistrationComposable(
            showCustomMessage = state.showCustomMessage,
            message = state.message,
            isError = state.isError,
            onLogin = { email, password -> controller.login(email, password) },
            onSignUp = { email, password, username ->
                controller.signUp(
                    email,
                    password,
                    username
                )
            },
            onMessageDismiss = { controller.dismissMessage() },
            resetPassword = { email -> controller.sendEmailToResetPassword(email) }
        )
    }
}