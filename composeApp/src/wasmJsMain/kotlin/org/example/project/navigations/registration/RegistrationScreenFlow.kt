package org.example.project.navigations.registration

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.example.project.navController.NavController
import org.example.project.navController.WebScreen
import org.example.project.registration.RegistrationComposable
import org.example.project.registration.di.registrationModule
import org.example.project.registration.state.RegistrationEvents
import org.example.project.registration.state.RegistrationUiEvents
import org.example.project.registration.viewModel.WebRegistrationController
import org.koin.compose.koinInject
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules

@Composable
fun RegistrationScreenFlow(navController: NavController) {
    loadKoinModules(registrationModule)

    val controller = koinInject<WebRegistrationController>()
    val state by controller.state.collectAsState()

    LaunchedEffect(state.uiEvent) {
        when (state.uiEvent) {
            RegistrationUiEvents.NavigateToTutorFlow -> {
                navController.navigate(WebScreen.Tutor)
                unloadKoinModules(registrationModule)
                controller.onEvent(RegistrationEvents.ClearUiEvent)
            }

            else -> {}
        }
    }

    RegistrationComposable(
        showCustomMessage = state.showCustomMessage,
        message = state.message,
        isError = state.isError,
        onLogin = { email, password ->
            controller.onEvent(RegistrationEvents.Login(email, password))
        },
        onSignUp = { email, pass, username ->
            controller.onEvent(RegistrationEvents.SignUp(email, pass, username))
        },
        onMessageDismiss = {
            controller.onEvent(RegistrationEvents.InitializeStateAfterShowingMessage)
        },
        resetPassword = { email ->
            controller.onEvent(RegistrationEvents.ResetPassword(email))
        },
        isWebUi = true
    )
}
