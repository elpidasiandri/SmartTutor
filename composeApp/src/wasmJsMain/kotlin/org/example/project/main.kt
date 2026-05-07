@file:Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")

package org.example.project

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import org.example.project.navController.NavController
import org.example.project.navController.WebScreen
import org.example.project.navController.di.navigationModule
import org.example.project.navigations.NavigateToScreen
import org.example.project.registration.AuthUserHolder
import org.example.project.registration.firebase.initFirebase
import org.example.project.registration.firebase.onAuthStateChanged
import org.example.project.storage.WebStorageHelper
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin

@OptIn(ExperimentalComposeUiApi::class)
object WebApp : KoinComponent {
    fun start() {
        startKoin {
            modules(navigationModule)
        }

        val navController: NavController by inject()
        val auth = initFirebase().auth

        onAuthStateChanged(auth) { user ->
            if (user != null) {
                AuthUserHolder.currentUser = user
                navController.navigate(WebScreen.Tutor)
            } else {
                navController.navigate(WebScreen.Registration)
            }
        }
        ComposeViewport("tutorApp") {
            val screen by navController.screen.collectAsState()
            NavigateToScreen(screen, navController)
        }
    }
}

fun main() = WebApp.start()