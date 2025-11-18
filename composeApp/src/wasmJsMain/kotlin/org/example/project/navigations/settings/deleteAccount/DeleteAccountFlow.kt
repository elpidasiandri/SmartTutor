package org.example.project.navigations.settings.deleteAccount

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.example.project.navController.NavController
import org.example.project.navController.WebScreen
import org.example.project.settings.deleteAccount.DeleteAccountScreen
import org.example.project.settings.deleteAccount.di.deleteAccountModule
import org.example.project.settings.deleteAccount.state.DeleteAccountEvents
import org.example.project.settings.deleteAccount.state.DeleteAccountUiEvents
import org.example.project.settings.deleteAccount.viewModel.DeleteAccountController
import org.example.project.storage.WebStorageHelper.clearToken
import org.koin.compose.koinInject
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules

@Composable
fun DeleteAccountFlow(navController: NavController) {
    loadKoinModules(deleteAccountModule)

    val controller = koinInject<DeleteAccountController>()
    val state by controller.state.collectAsState()

    LaunchedEffect(state.event) {
        when (state.event) {
            DeleteAccountEvents.AccountDeleted -> {
                controller.onEvent(DeleteAccountUiEvents.InitializeStateAfterShowingMessage)
                clearToken()
                unloadKoinModules(deleteAccountModule)
                navController.navigate(WebScreen.Registration)
            }

            DeleteAccountEvents.NavigateBack -> {
                controller.onEvent(DeleteAccountUiEvents.InitializeStateAfterShowingMessage)
                unloadKoinModules(deleteAccountModule)
                navController.navigate(WebScreen.Settings)
            }

            else -> {}
        }
    }

    DeleteAccountScreen(
        isLoading = state.isLoading,
        showCustomMessage = state.showCustomMessage,
        message = state.message,
        isError = state.isError,
        onBack = { controller.onEvent(DeleteAccountUiEvents.NavigateBack) },
        onDeleteConfirmed = {
            controller.onEvent(DeleteAccountUiEvents.DeleteAccount)
        },
        onCancel = { controller.onEvent(DeleteAccountUiEvents.NavigateBack) },
        onMessageDismiss = {
            controller.onEvent(DeleteAccountUiEvents.InitializeStateAfterShowingMessage)
        }
    )
}
