package org.example.project.navigations.settings.changePassword

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import kotlinx.coroutines.delay
import org.example.project.navController.NavController
import org.example.project.navController.WebScreen
import org.example.project.settings.changePassword.ChangePasswordSettingsScreen
import org.example.project.settings.changePassword.di.changePasswordModule
import org.example.project.settings.changePassword.state.ChangePasswordEvents
import org.example.project.settings.changePassword.state.ChangePasswordUiEvents
import org.example.project.settings.changePassword.viewModel.ChangePasswordController
import org.koin.compose.koinInject
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules

@Composable
fun ChangePasswordFlow(navController: NavController) {
    loadKoinModules(changePasswordModule)

    val controller = koinInject<ChangePasswordController>()
    val state by controller.state.collectAsState()

    LaunchedEffect(state.event) {
        when (state.event) {
            ChangePasswordEvents.NavigateBack -> {
                unloadKoinModules(changePasswordModule)
                navController.navigate(WebScreen.Settings)
            }

            ChangePasswordEvents.Result -> {
                delay(2000)
                controller.onEvent(ChangePasswordUiEvents.Initialize)
                unloadKoinModules(changePasswordModule)
                navController.navigate(WebScreen.Settings)
            }

            else -> {}
        }
    }

    ChangePasswordSettingsScreen(
        showCustomMessage = state.showCustomMessage,
        message = state.message,
        isError = state.isError,
        isLoading = state.isLoading,
        onBack = { controller.onEvent(ChangePasswordUiEvents.NavigateBack) },
        changePassword = { old, new ->
            controller.onEvent(ChangePasswordUiEvents.ChangePassword(old, new))
        },
        onMessageDismiss = {
            controller.onEvent(ChangePasswordUiEvents.InitializeStateAfterShowingMessage)
        }
    )
}
