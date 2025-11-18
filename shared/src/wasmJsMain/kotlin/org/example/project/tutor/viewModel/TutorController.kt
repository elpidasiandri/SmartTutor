package org.example.project.tutor.viewModel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.project.settings.logout.useCase.LogoutUseCase
import org.example.project.strings.SmartTutorStrings
import org.example.project.tutor.TutorEvents
import org.example.project.tutor.TutorState
import org.example.project.tutor.TutorUiEvents
import org.example.project.tutor.speakText
import org.example.project.tutor.startVoiceRecognition
import org.example.project.tutor.stopVoiceRecognition
import org.example.project.utils.EmptyValues

class TutorController(
    private val logoutUseCase: LogoutUseCase,
) {
    private val scope = CoroutineScope(Dispatchers.Default)

    private val _state = MutableStateFlow(TutorState())
    val state: StateFlow<TutorState> = _state
    fun onEvent(event: TutorUiEvents) {
        when (event) {
            TutorUiEvents.Logout -> {
                logOutUser()
            }

            TutorUiEvents.InitializeStateAfterShowingMessage -> {
                initializeStateAfterShowingMessage()
            }

            TutorUiEvents.ClearUiEvent -> {
                clearUiEvent()
            }

            else -> {}
        }
    }

    private fun logOutUser() {
        scope.launch {
            logoutUseCase { success, _ ->
                if (success) {
                    _state.update {
                        it.copy(
                            event = TutorEvents.NavigateToLogOut
                        )
                    }
                } else {
                    stateForErrorMessage()
                }
            }
        }
    }

    private fun initializeStateAfterShowingMessage() {
        _state.update {
            it.copy(
                showCustomMessage = false,
                message = EmptyValues.EMPTY_STRING,
                isError = false
            )
        }
    }

    private fun clearUiEvent() {
        _state.update {
            it.copy(
                uiEvent = TutorUiEvents.None,
                event = TutorEvents.None
            )
        }
    }

    private fun stateForErrorMessage(message: String? = null) {
        _state.update {
            it.copy(
                showCustomMessage = true,
                message = message ?: SmartTutorStrings.generic_error,
                isError = true
            )
        }
    }

    fun playAIMessage(text: String) {
        val normalized = normalizeTranscript(text)
        speakText(normalized)
    }

    fun startVoiceInput(onResult: (String) -> Unit) {
        println("Voice recognition started")
        startVoiceRecognition(onResult)
    }

    private fun normalizeTranscript(text: String): String {
        return text.replace("-", "")
            .replace(Regex("\\s+"), " ")
            .trim()
    }


    fun stopVoiceInput() {
        stopVoiceRecognition()
    }
}
