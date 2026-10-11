package org.example.project.navigations.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.example.project.experiment.ExperimentConfig
import org.example.project.navController.NavController
import org.example.project.navController.WebScreen
import org.example.project.registration.AuthUserHolder
import org.example.project.settings.SettingsEvents
import org.example.project.settings.SettingsScreen
import org.example.project.settings.SettingsUiEvents
import org.example.project.settings.settingsView.di.settingsModule
import org.example.project.settings.settingsView.viewModel.SettingsController
import org.koin.compose.koinInject
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules

@Composable
fun SettingsScreenFlow(navController: NavController) {

    loadKoinModules(settingsModule)

    val controller = koinInject<SettingsController>()
    val state by controller.state.collectAsState()

    LaunchedEffect(state.eventUi) {
        when (state.eventUi) {
            SettingsUiEvents.NavigateToDeleteAccount -> {
                unloadKoinModules(settingsModule)
                navController.navigate(WebScreen.DeleteAccountSettings)
            }

            SettingsUiEvents.NavigateToChangePassword -> {
                unloadKoinModules(settingsModule)
                navController.navigate(WebScreen.ChangePasswordSettings)
            }

            SettingsUiEvents.LeaveSettingsFlow -> {
                controller.onEvent(SettingsEvents.Initialize)
                unloadKoinModules(settingsModule)
                navController.navigate(WebScreen.Tutor)
            }

            else -> {}
        }
    }
    val user = AuthUserHolder.currentUser

    val isExperimentUser =
        user?.emailVerified == true &&
                ExperimentConfig.shouldUseExperimentDocument(user.email)
    SettingsScreen(
        deleteAccountVisible = false,
        onBack = { controller.onEvent(SettingsEvents.LeaveSettingsFlow) },
        onChangePasswordClick = {
            controller.onEvent(SettingsEvents.NavigateToChangePassword)
        },
        onDeleteAccountClick = {
//            controller.onEvent(SettingsEvents.NavigateToDeleteAccount)
        },
        showLanguageSelector = !isExperimentUser,
    )
}
