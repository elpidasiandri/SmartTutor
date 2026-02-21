package org.example.project.navigations.tutor

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.example.project.navController.NavController
import org.example.project.navController.WebScreen
import org.example.project.tutor.TutorChatUI
import org.example.project.tutor.state.TutorEvents
import org.example.project.tutor.state.TutorUiEvents
import org.example.project.tutor.di.tutorModule
import org.example.project.tutor.pdfPicker.WebPdfPicker
import org.example.project.tutor.viewModel.TutorController
import org.example.project.utils.validation.PdfValidationResult
import org.example.project.utils.validation.validatePdf
import org.koin.compose.koinInject
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules

@Composable
fun WebTutorScreenFlow(navController: NavController) {
    loadKoinModules(tutorModule)

    val controller = koinInject<TutorController>()
    val state by controller.state.collectAsState()
    val pdfPicker = WebPdfPicker()
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
        playAIMessage = { text -> controller.playAIMessage(text) },
        startVoiceInput = { controller.startVoiceInput(it) },
        stopVoiceInput = { controller.stopVoiceInput() },
        showCustomMessage = state.showCustomMessage,
        widthDividerMenu = 4,
        message = state.message,
        isError = state.isError,
        onSettingsClick = { navController.navigate(WebScreen.Settings) },
        onLogoutClick = { controller.onEvent(TutorUiEvents.Logout) },
        onMessageDismiss = {
            controller.onEvent(TutorUiEvents.InitializeStateAfterShowingMessage)
        },
        chatData = state.session,
        chatHistory = state.chatHistory,
        onUploadPdfClick = {
            CoroutineScope(Dispatchers.Default).launch {
                val file = pdfPicker.pickPdf()
                file?.let { fileNotNull ->
                    when (val result = validatePdf(fileNotNull)) {
                        is PdfValidationResult.Error -> {
                            controller.onEvent(TutorUiEvents.ShowErrorMessage(result.message))
                        }

                        is PdfValidationResult.Success -> {
                            controller.onEvent(TutorUiEvents.UploadPdf(result.file))
                            println("PDF Success: ${result.file.name}, ${result.file.sizeBytes}")
                        }
                    }
                }
            }
        },
        selectLearningUnits = { luId ->
            val units = state.allUnits
            val index = units.indexOfFirst { it.id == luId }
            if (index >= 0) controller.onEvent(TutorUiEvents.OpenUnit(index))
        },
        onNextUnitClick = { controller.onEvent(TutorUiEvents.NextUnit) },
        onExercisesClick = { },
        startNewChat = {
            controller.onEvent(TutorUiEvents.NewChat)
        },
        deleteSubject = { subjectId ->
            controller.onEvent(TutorUiEvents.DeleteMaterial(subjectId))
        },
        isWebFlow = true
    )
}
