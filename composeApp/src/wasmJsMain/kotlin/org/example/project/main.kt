@file:Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")

package org.example.project

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import org.example.project.registration.RegistrationComposable
import org.example.project.registration.di.registrationModule
import org.example.project.registration.state.RegistrationState
import org.example.project.registration.state.RegistrationUiEvents
import org.example.project.registration.viewModel.WebRegistrationController
import org.example.project.storage.WebStorageHelper
import org.example.project.tutor.TutorChatUI
import org.example.project.tutor.viewModel.TutorController
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
        val tutorController = TutorController()

        if (WebStorageHelper.isTokenValid()) {
            appController.goToTutor()
        }
        ComposeViewport("tutorApp") {
            val state by controller.state.collectAsState()
            setUpViewModelEvents(state, appController, controller)
            navigateToScreen(appController, state, controller, tutorController)
        }
    }
}

@OptIn(ExperimentalWasmJsInterop::class)
@Composable
private fun navigateToScreen(
    appController: AppController,
    state: RegistrationState,
    controller: WebRegistrationController,
    tutorController: TutorController,
) {
    val screen by appController.screen
    var userMessage by remember { mutableStateOf("") }
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

        Screen.Tutor -> {
            TutorChatUI(
                userMessage = userMessage,
                onMessageChange = { userMessage = it },
                playAIMessage = { text -> tutorController.playAIMessage(text) },
                startVoiceInput = { onResult -> tutorController.startVoiceInput(onResult) },
                stopVoiceInput = { tutorController.stopVoiceInput() }
            )
        }
        //TutorComposable()
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