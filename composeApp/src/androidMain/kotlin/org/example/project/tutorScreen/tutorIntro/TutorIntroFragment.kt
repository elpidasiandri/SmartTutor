package org.example.project.tutorScreen.tutorIntro

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import org.example.project.myProgress.MyProgressActivity
import org.example.project.registrationScreen.RegistrationActivity
import org.example.project.settings.SettingsActivity
import org.example.project.storage.StorageHelper.clearToken
import org.example.project.tutor.AndroidPdfPicker
import org.example.project.tutor.TutorScreen
import org.example.project.tutor.state.TutorEvents
import org.example.project.tutor.state.TutorUiEvents
import org.example.project.tutor.module.tutorModule
import org.example.project.tutor.viewModel.TutorIntroViewModel
import org.example.project.utils.validation.PdfValidationResult
import org.example.project.utils.validation.validatePdf
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules

class TutorIntroFragment : Fragment() {
    private val viewModel by activityViewModel<TutorIntroViewModel>()
    private lateinit var pdfPicker: AndroidPdfPicker

    private val settingsLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        loadKoinModules(tutorModule)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {

        pdfPicker = AndroidPdfPicker(this)

        setUpObserver()
        return ComposeView(requireContext()).apply {
            setContent {
                val state by viewModel.state.collectAsStateWithLifecycle()

                var userMessage by remember { mutableStateOf("") }
                TutorScreen(
                    chatData = state.session,
                    chatHistory = state.chatHistory,
                    showCustomMessage = state.showCustomMessage,
                    message = state.message,
                    isError = state.isError,
                    userMessage = userMessage,
                    onSettingsClick = {
                        settingsLauncher.launch(SettingsActivity.newIntent(requireContext()))
                    },
                    onLogoutClick = { viewModel.onEvent(TutorUiEvents.Logout) },
                    onMessageDismiss = {
                        viewModel.onEvent(TutorUiEvents.InitializeStateAfterShowingMessage)
                    },
                    onUploadPdfClick = {
                        uploadPdfFile()
                    },
                    selectLearningUnit = { title, subjectId ->
                        viewModel.onEvent(TutorUiEvents.SelectLearningUnit(title, subjectId))
                    },
                    onNextUnitClick = { viewModel.onEvent(TutorUiEvents.NextUnit) },
                    onExercisesClick = { },
                    startNewChat = {
                        viewModel.onEvent(TutorUiEvents.NewChat)
                    },
                    deleteSubject = { subjectId ->
                        viewModel.onEvent(TutorUiEvents.DeleteChat(subjectId))
                    },
                    selectedTab = state.selectedTab,
                    onChangeTab = { viewModel.onEvent(TutorUiEvents.ChangeTab(it)) },
                    tabInfoMessage = state.tabInfoMessage,
                    currentUnit = state.currentUnit,
                    onSendMessage = { message ->
                        viewModel.onEvent(TutorUiEvents.SendMessage(message))
                    },
                    openExistingChat = { chatId ->
                        viewModel.onEvent(TutorUiEvents.OpenExistingChat(chatId))
                    },
                    navigateToMyProgress = {
                        viewModel.onEvent(TutorUiEvents.OpenMyProgress)
                    },
                    canGoNext = state.canGoNext,
                    askTutor = state.askTutor,
                    onExerciseAnswered = { index, type, selected, correct, isCorrect, timeMs, question ->
                        viewModel.onEvent(
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
                    }
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unloadKoinModules(tutorModule)
    }

    private fun uploadPdfFile() {
        viewLifecycleOwner.lifecycleScope.launch {
            val file = pdfPicker.pickPdf() ?: return@launch

            when (val result = validatePdf(file)) {
                is PdfValidationResult.Error -> {
                    viewModel.onEvent(TutorUiEvents.ShowErrorMessage(result.message))
                }

                is PdfValidationResult.Success -> {
                    viewModel.onEvent(TutorUiEvents.UploadPdf(result.file))
                }
            }
        }
    }

    private fun setUpObserver() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { event ->
                    when (event.event) {
                        TutorEvents.NavigateToLogOut -> {
                            if (isAdded) {
                                clearToken(requireContext())
                                startActivity(
                                    Intent(
                                        requireContext(),
                                        RegistrationActivity::class.java
                                    )
                                )
                                requireActivity().finish()
                            }
                        }

                        TutorEvents.NavigateToMyProgress -> {
                            if (isAdded) {
                                startActivity(
                                    Intent(
                                        requireContext(),
                                        MyProgressActivity::class.java
                                    )
                                )
                                viewModel.common.setEventNone()
                            }
                        }

                        else -> {}
                    }
                }
            }
        }
    }

    companion object {
        fun newInstance(): TutorIntroFragment {
            return TutorIntroFragment().apply {
                arguments = Bundle().apply {}
            }
        }
    }
}
