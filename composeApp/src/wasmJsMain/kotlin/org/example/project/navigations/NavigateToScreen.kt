package org.example.project.navigations

import androidx.compose.runtime.Composable
import org.example.project.navController.NavController
import org.example.project.navController.WebScreen
import org.example.project.navigations.registration.RegistrationScreenFlow
import org.example.project.navigations.settings.changePassword.ChangePasswordFlow
import org.example.project.navigations.settings.SettingsScreenFlow
import org.example.project.navigations.settings.deleteAccount.DeleteAccountFlow
import org.example.project.navigations.tutor.TutorScreenFlow
@Composable
fun NavigateToScreen(
    screen: WebScreen,
    navController: NavController,
) {
    when (screen) {
        WebScreen.Registration -> {
            RegistrationScreenFlow(navController)
        }

        WebScreen.Tutor -> {
            TutorScreenFlow(navController)
        }

        WebScreen.Settings -> {
            SettingsScreenFlow(navController)
        }

        WebScreen.ChangePasswordSettings -> {
            ChangePasswordFlow(navController)
        }

        WebScreen.DeleteAccountSettings -> {
            DeleteAccountFlow(navController)
        }
    }
}
