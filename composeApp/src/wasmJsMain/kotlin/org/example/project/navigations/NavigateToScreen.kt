package org.example.project.navigations

import androidx.compose.runtime.Composable
import org.example.project.navController.NavController
import org.example.project.navController.WebScreen
import org.example.project.navigations.SplashScreen.SplashScreenFlow
import org.example.project.navigations.myprogress.MyProgressFlow
import org.example.project.navigations.registration.RegistrationScreenFlow
import org.example.project.navigations.settings.changePassword.ChangePasswordFlow
import org.example.project.navigations.settings.SettingsScreenFlow
import org.example.project.navigations.settings.deleteAccount.DeleteAccountFlow
import org.example.project.navigations.tutor.WebTutorScreenFlow
import androidx.compose.runtime.LaunchedEffect
import org.example.project.tutor.viewModel.TutorController
@Composable
fun NavigateToScreen(
    screen: WebScreen,
    navController: NavController,
    tutorController: TutorController?,
) {
    when (screen) {
        WebScreen.Registration -> {
            RegistrationScreenFlow(navController)
        }

        WebScreen.Tutor -> {
            if (tutorController != null) {
                WebTutorScreenFlow(
                    navController = navController,
                    controller = tutorController
                )
            } else {
                LaunchedEffect(Unit) {
                    navController.navigate(WebScreen.Registration)
                }
            }
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

        WebScreen.MyProgress -> {
            MyProgressFlow(navController)
        }

        WebScreen.Splash -> {
            SplashScreenFlow()
        }
    }
}
