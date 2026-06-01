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
import org.example.project.registration.AuthUserHolder
import org.example.project.tutor.TutorScreen
import org.example.project.tutor.models.domainModels.tutorChat.TutorTab
import org.example.project.tutor.state.TutorEvents
import org.example.project.tutor.state.TutorUiEvents
import org.example.project.tutor.pdfPicker.WebPdfPicker
import org.example.project.tutor.viewModel.TutorController
import org.example.project.utils.validation.PdfValidationResult
import org.example.project.utils.validation.validatePdf
import org.koin.compose.getKoin
import org.koin.core.parameter.parametersOf

@Composable
fun WebTutorScreenFlow(navController: NavController) {
    val user = AuthUserHolder.currentUser

    if (user == null) {
        navController.navigate(WebScreen.Registration)
        return
    }

    val koin = getKoin()

    val controller = remember(user.uid) {
        koin.get<TutorController> {
            parametersOf(user.uid)
        }
    }
    LaunchedEffect(user.uid) {
        controller.start()
    }
    val state by controller.state.collectAsState()
    val pdfPicker = WebPdfPicker()
    var userMessage by remember { mutableStateOf("") }



    if (user == null) {
        navController.navigate(WebScreen.Registration)
        return
    }
    LaunchedEffect(state.event) {
        when (state.event) {
            TutorEvents.NavigateToLogOut -> {
                controller.onEvent(TutorUiEvents.ClearUiEvent)
                navController.navigate(WebScreen.Registration)
            }

            else -> {}
        }
    }
    TutorScreen(
        chatData = state.session,
        chatHistory = state.chatHistory,
        showCustomMessage = state.showCustomMessage,
        message = state.message,
        isError = state.isError,
        userMessage = userMessage,
        onSettingsClick = {
            navController.navigate(WebScreen.Settings)
        },
        onLogoutClick = { controller.onEvent(TutorUiEvents.Logout) },
        onMessageDismiss = {
            controller.onEvent(TutorUiEvents.InitializeStateAfterShowingMessage)
        },
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
                        }
                    }
                }
            }
        },
        selectLearningUnit = { title, subjectId ->
            controller.onEvent(TutorUiEvents.SelectLearningUnit(title, subjectId))
        },
        onNextUnitClick = { controller.onEvent(TutorUiEvents.NextUnit) },
        onExercisesClick = { },
        startNewChat = {
            controller.onEvent(TutorUiEvents.NewChat)
        },
        deleteSubject = { subjectId ->
            controller.onEvent(TutorUiEvents.DeleteChat(subjectId))
        },
        selectedTab = state.selectedTab,
        onChangeTab = { controller.onEvent(TutorUiEvents.ChangeTab(it)) },
        tabInfoMessage = state.tabInfoMessage,
        currentUnit = state.currentUnit,
        onSendMessage = { message ->
            controller.onEvent(TutorUiEvents.SendMessage(message))
        },

        openExistingChat = { chatId ->
            controller.onEvent(TutorUiEvents.OpenExistingChat(chatId))
        },
        navigateToMyProgress = {
            navController.navigate(WebScreen.MyProgress)
        },
        canGoNext = state.canGoNext,
        askTutor = state.askTutor,
        onExerciseAnswered = { index, type, selected, correct, isCorrect, timeMs, question ->
            controller.onEvent(
                TutorUiEvents.ExerciseAnswered(
                    exerciseIndex = index,
                    exerciseType = type,
                    selectedAnswer = selected,
                    correctAnswer = correct,
                    isCorrect = isCorrect,
                    timeToAnswerMs = timeMs,
                    question = question,
                )
            )
        },
        onAskTutorForOpenExercise = { exerciseIndex, question, userAnswer, suggestedSolution ->
            controller.onEvent(
                TutorUiEvents.AskTutorForOpenExercise(
                    exerciseIndex = exerciseIndex,
                    question = question,
                    userAnswer = userAnswer,
                    suggestedSolution = suggestedSolution,
                )
            )
        },
        exerciseTutorAnswers = state.exerciseTutorAnswers,
        answeredExerciseKeys = state.answeredExerciseKeys,
        currentIndex = state.currentIndex,
        onMoreExercisesClick = {
            controller.onEvent(
                TutorUiEvents.LoadMoreExercises
            )
        },
        isLoadingMoreExercises = state.askTutor,
        selectedExerciseIndex = state.selectedExerciseIndex,
        onLearningClick = { title, subjectId ->
            controller.onEvent(
                TutorUiEvents.OpenUnitSection(
                    exerciseIndex = null,
                    title = title,
                    subjectId = subjectId,
                    tab = TutorTab.Learning,
                )
            )

        },
        onExercisesMenuTabClick = { title, subjectId, exerciseIndex ->
            controller.onEvent(
                TutorUiEvents.OpenUnitSection(
                    exerciseIndex = exerciseIndex,
                    title = title,
                    subjectId = subjectId,
                    tab = TutorTab.Exercises,
                )
            )
        },
    )
}
