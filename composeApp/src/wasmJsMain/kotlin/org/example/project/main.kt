@file:Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")

package org.example.project

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import org.example.project.tutor.di.tutorModule
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin

@OptIn(ExperimentalComposeUiApi::class)
object WebApp : KoinComponent {
    fun start() {
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

            if (user != null && user.emailVerified) {
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