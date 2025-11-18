package org.example.project.navigations.tutor

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import org.example.project.navController.NavController
import org.example.project.navController.WebScreen
import org.example.project.tutor.TutorChatUI
import org.example.project.tutor.TutorEvents
import org.example.project.tutor.TutorUiEvents
import org.example.project.tutor.di.tutorModule
import org.example.project.tutor.viewModel.TutorController
import org.koin.compose.koinInject
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules

@Composable
fun TutorScreenFlow(navController: NavController) {
    loadKoinModules(tutorModule)

    val controller = koinInject<TutorController>()
    val state by controller.state.collectAsState()

    var userMessage by remember { mutableStateOf("") }

    LaunchedEffect(state.event) {
        when (state.event) {
            TutorEvents.NavigateToLogOut -> {
                controller.onEvent(TutorUiEvents.ClearUiEvent)
                unloadKoinModules(tutorModule)
                navController.navigate(WebScreen.Registration)
            }
            else -> {}
        }
    }

    TutorChatUI(
        userMessage = userMessage,
        onMessageChange = { userMessage = it },
        playAIMessage = { controller.playAIMessage(it) },
        startVoiceInput = { controller.startVoiceInput(it) },
        stopVoiceInput = { controller.stopVoiceInput() },
        showCustomMessage = state.showCustomMessage,
        widthDividerMenu = 4,
        message = state.message,
        isError = state.isError,
        onSettingsClick = { navController.navigate(WebScreen.Settings) },
        onProgressClick = {},
        onLogoutClick = { controller.onEvent(TutorUiEvents.Logout) },
        onMessageDismiss = {
            controller.onEvent(TutorUiEvents.InitializeStateAfterShowingMessage)
        }
    )
}
