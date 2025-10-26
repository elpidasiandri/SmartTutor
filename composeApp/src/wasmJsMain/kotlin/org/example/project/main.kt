@file:Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")

package org.example.project

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import org.example.project.registration.RegistrationComposable
import org.example.project.registration.di.registrationModule
import org.example.project.registration.viewModel.WebRegistrationController
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin

@OptIn(ExperimentalComposeUiApi::class)
object WebApp : KoinComponent {

    fun start() {
        startKoin {
            modules(registrationModule)
        }

        val controller: WebRegistrationController by inject()

        ComposeViewport("tutorApp") {
            val state by controller.state.collectAsState()

            RegistrationComposable(
                showCustomMessage = state.showCustomMessage,
                message = state.message,
                isError = state.isError,
                onLogin = { email, password -> controller.login(email, password) },
                onSignUp = { email, password, username ->
                    controller.signUp(email, password, username)
                },
                onMessageDismiss = { controller.dismissMessage() },
                resetPassword = { email -> controller.sendEmailToResetPassword(email) }
            )
        }
    }
}

fun main() = WebApp.start()