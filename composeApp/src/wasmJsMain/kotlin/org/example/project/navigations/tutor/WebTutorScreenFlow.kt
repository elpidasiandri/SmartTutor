package org.example.project.navigations.tutor

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.example.project.components.loading.LoadingScreen
import org.example.project.navController.NavController
import org.example.project.navController.WebScreen
import org.example.project.registration.AuthUserHolder
import org.example.project.tutor.TutorScreen
import org.example.project.tutor.models.domainModels.file.PickedFile
import org.example.project.tutor.models.domainModels.tutorChat.TutorTab
import org.example.project.tutor.pdfPicker.WebPdfDropHandler
import org.example.project.tutor.state.TutorEvents
import org.example.project.tutor.state.TutorUiEvents
import org.example.project.tutor.pdfPicker.WebPdfPicker
import org.example.project.tutor.viewModel.TutorController
import org.example.project.utils.validation.PdfValidationResult
import org.example.project.utils.validation.validatePdf
import org.example.project.experiment.ExperimentConfig
import org.example.project.strings.SmartTutorStrings.drop_pdf
import org.example.project.strings.SmartTutorStrings.pdf_allowed
import org.example.project.strings.SmartTutorStrings.release_upload_pdf
import org.example.project.tutor.LocalIsExperimentUser
@Composable
fun WebTutorScreenFlow(
    navController: NavController,
    controller: TutorController,
) {
    val user = AuthUserHolder.currentUser

    if (user == null) {
        navController.navigate(WebScreen.Registration)
        return
    }

    val state by controller.state.collectAsState()
    val pdfPicker = WebPdfPicker()
    var userMessage by remember { mutableStateOf("") }
    var isDraggingFile by remember {
        mutableStateOf(false)
    }
    val isExperimentUser =
        user.emailVerified &&
                ExperimentConfig.shouldUseExperimentDocument(user.email)

    fun handlePickedPdf(file: PickedFile) {
        if (isExperimentUser) return
        when (val result = validatePdf(file)) {

            is PdfValidationResult.Error -> {
                controller.onEvent(
                    TutorUiEvents.ShowErrorMessage(result.message)
                )
            }

            is PdfValidationResult.Success -> {
                controller.onEvent(
                    TutorUiEvents.UploadPdf(result.file)
                )
            }
        }
    }


    DisposableEffect(
        user.uid,
        isExperimentUser,
        state.isInitializing
    ) {
        val dropHandler = WebPdfDropHandler(
            onFileDropped = { browserFile ->
                if (!isExperimentUser && !state.isInitializing) {
                    CoroutineScope(Dispatchers.Default).launch {
                        val pickedFile = pdfPicker.convertFile(browserFile)

                        if (pickedFile != null) {
                            handlePickedPdf(pickedFile)
                        } else {
                            controller.onEvent(
                                TutorUiEvents.ShowErrorMessage(pdf_allowed)
                            )
                        }
                    }
                }
            },
            onDragStateChanged = { dragging ->
                isDraggingFile = dragging && !isExperimentUser && !state.isInitializing
            },
            onInvalidDrop = { message ->
                if (!isExperimentUser && !state.isInitializing) {
                    controller.onEvent(
                        TutorUiEvents.ShowErrorMessage(message)
                    )
                }
            }
        )

        dropHandler.start()

        onDispose {
            dropHandler.stop()
        }
    }


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

    if (state.isInitializing) {
        LoadingScreen(
            backgroundColor = Color.White
        )
        return
    }

    if (isExperimentUser && state.activeChatId.isBlank()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Δεν ήταν δυνατή η φόρτωση του εκπαιδευτικού υλικού."
            )
        }
        return
    }

    CompositionLocalProvider(
        LocalIsExperimentUser provides isExperimentUser
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
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
                    if (!isExperimentUser) {
                        CoroutineScope(Dispatchers.Default).launch {
                            pdfPicker.pickPdf()?.let { file ->
                                handlePickedPdf(file)
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
                onExerciseAnswered = { index, type, selected, correct, isCorrect, timeMs, question, maxPoints ->
                    controller.onEvent(
                        TutorUiEvents.ExerciseAnswered(
                            exerciseIndex = index,
                            exerciseType = type,
                            selectedAnswer = selected,
                            correctAnswer = correct,
                            isCorrect = isCorrect,
                            timeToAnswerMs = timeMs,
                            question = question,
                            maxPoints = maxPoints,
                        )
                    )
                },
                onAskTutorForOpenExercise = { exerciseIndex, question, userAnswer, suggestedSolution, maxPoint ->
                    controller.onEvent(
                        TutorUiEvents.AskTutorForOpenExercise(
                            exerciseIndex = exerciseIndex,
                            question = question,
                            userAnswer = userAnswer,
                            suggestedSolution = suggestedSolution,
                            maxPoints = maxPoint
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
                exercisesLoading = state.exercisesLoading,
                pdfProcessingProgress = state.pdfProcessingProgress,
            )
            if (isDraggingFile) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .zIndex(10f)
                        .background(Color(0xE6241B3C)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = drop_pdf,
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = release_upload_pdf,
                            color = Color.White,
                            modifier = Modifier.padding(top = 12.dp)
                        )
                    }
                }
            }
        }
    }
}
