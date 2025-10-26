@file:Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")

package org.example.project

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import org.example.project.registration.RegistrationComposable
import org.example.project.registration.di.registrationModule
import org.example.project.registration.state.RegistrationState
import org.example.project.registration.state.RegistrationUiEvents
import org.example.project.registration.viewModel.WebRegistrationController
import org.example.project.tutor.TutorComposable
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
        val appController = AppController()
        ComposeViewport("tutorApp") {
            val state by controller.state.collectAsState()
            setUpViewModelEvents(state, appController, controller)
            navigateToScreen(appController, state, controller)
        }
    }
}

@Composable
private fun navigateToScreen(
    appController: AppController,
    state: RegistrationState,
    controller: WebRegistrationController,
) {
    val screen by appController.screen

    when (screen) {
        Screen.Registration -> {

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

        Screen.Tutor -> TutorComposable()
    }
}

@Composable
private fun setUpViewModelEvents(
    state: RegistrationState,
    appController: AppController,
    controller: WebRegistrationController,
) {
    LaunchedEffect(state.uiEvent) {
        when (state.uiEvent) {
            RegistrationUiEvents.NavigateToTutorFlow -> {
                appController.goToTutor()
                controller.clearUiEvent()
            }

            else -> {}
        }
    }
}

fun main() = WebApp.start()