@file:Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")

package org.example.project
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import org.example.project.experiment.ExperimentConfig
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import org.example.project.myProgress.di.myProgressModule
import org.example.project.navController.NavController
import org.example.project.navController.WebScreen
import org.example.project.navController.di.navigationModule
import org.example.project.navigations.NavigateToScreen
import org.example.project.registration.AuthUserHolder
import org.example.project.registration.firebase.FirebaseContext
import org.example.project.registration.firebase.module.firebaseModule
import org.example.project.registration.firebase.onAuthStateChanged
import org.example.project.strings.LanguageManager
import org.example.project.strings.WebLanguageStorage
import org.example.project.tutor.di.tutorModule
import org.example.project.tutor.viewModel.TutorController
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalComposeUiApi::class)
object WebApp : KoinComponent {
    fun start() {
        LanguageManager.initialize(
            WebLanguageStorage()
        )

        startKoin {
            modules(
                firebaseModule,
                navigationModule,
                tutorModule,
                myProgressModule
            )
        }

        val navController: NavController by inject()
        val firebase: FirebaseContext by inject()
        val auth = firebase.auth

        onAuthStateChanged(auth) { user ->
            AuthUserHolder.currentUser = user

            val isAuthenticated =
                user != null && user.emailVerified

            val isExperimentUser =
                user?.emailVerified == true &&
                        ExperimentConfig.shouldUseExperimentDocument(user.email)

            LanguageManager.setExperimentMode(isExperimentUser)

            val currentScreen = navController.screen.value

            println(
                "AUTH_STATE: authenticated=$isAuthenticated, " +
                        "screen=$currentScreen"
            )

            when {
                !isAuthenticated -> {
                    if (currentScreen != WebScreen.Registration) {
                        navController.navigate(WebScreen.Registration)
                    }
                }

                currentScreen == WebScreen.Splash ||
                        currentScreen == WebScreen.Registration -> {
                    navController.navigate(WebScreen.Tutor)
                }
            }
        }


        ComposeViewport("tutorApp") {
            val screen by navController.screen.collectAsState()
            val user = AuthUserHolder.currentUser
            val koin = getKoin()

            val tutorController = if (
                user != null && user.emailVerified
            ) {
                remember(user.uid) {
                    koin.get<TutorController> {
                        parametersOf(user.uid)
                    }
                }
            } else {
                null
            }

            LaunchedEffect(tutorController) {
                if (tutorController != null && user != null) {
                    tutorController.start(
                        isExperimentUser =
                            ExperimentConfig.shouldUseExperimentDocument(
                                user.email
                            )
                    )
                }
            }

            DisposableEffect(tutorController) {
                onDispose {
                    tutorController?.close()
                }
            }

            NavigateToScreen(
                screen = screen,
                navController = navController,
                tutorController = tutorController
            )
        }

    }
}

fun main() = WebApp.start()