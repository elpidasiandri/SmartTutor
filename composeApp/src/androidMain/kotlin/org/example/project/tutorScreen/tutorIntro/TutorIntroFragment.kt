package org.example.project.tutorScreen.tutorIntro

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
import java.util.Locale

class TutorIntroFragment : Fragment() {
    private val viewModel by activityViewModel<TutorIntroViewModel>()
    private lateinit var textToSpeech: TextToSpeech
    private var speechRecognizer: SpeechRecognizer? = null
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
        textToSpeech = TextToSpeech(requireContext()) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.language = Locale.US
            }
        }
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
                    selectLearningUnit = { title ->
                        viewModel.onEvent(TutorUiEvents.SelectLearningUnit(title))
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
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unloadKoinModules(tutorModule)
        textToSpeech.stop()
        textToSpeech.shutdown()
        speechRecognizer = null
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

    private fun playAIMessage(message: String) {
        textToSpeech.speak(message, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    private fun startVoiceInput(onResult: (String) -> Unit) {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(requireContext())
        speechRecognizer?.setRecognitionListener(object : RecognitionListener {

            override fun onPartialResults(partialResults: Bundle?) {
                val matches =
                    partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    onResult(matches[0])
                }
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    onResult(matches[0])
                }
            }

            override fun onError(error: Int) {}

            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }

        speechRecognizer?.startListening(intent)
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

    private fun stopVoiceInput() {
        speechRecognizer?.stopListening()
        speechRecognizer?.cancel()
        speechRecognizer?.destroy()
        speechRecognizer = null
    }

    private fun checkAudioPermissions(onGranted: () -> Unit) {
        val permission = Manifest.permission.RECORD_AUDIO
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                permission
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(permission), 101)
        } else {
            onGranted()
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
