package org.example.project.registration.viewModel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.project.registration.state.RegistrationEvents
import org.example.project.registration.state.RegistrationState
import org.example.project.registration.state.RegistrationUiEvents
import org.example.project.registration.useCases.registration.RegistrationUseCase
import org.example.project.storage.WebStorageHelper
import org.example.project.strings.SmartTutorStrings
import org.example.project.strings.SmartTutorStrings.error_message_reset_password
import org.example.project.strings.SmartTutorStrings.success_message_reset_password
import org.example.project.utils.EmptyValues

class WebRegistrationController(
    private val registrationUseCases: RegistrationUseCase,
) {
    private val scope = CoroutineScope(Dispatchers.Default)
    private val _state = MutableStateFlow(RegistrationState())
    val state: StateFlow<RegistrationState> = _state

    fun onEvent(event: RegistrationEvents) {
        when (event) {
            RegistrationEvents.InitializeStateAfterShowingMessage -> {
                _state.update {
                    it.copy(
                        showCustomMessage = false,
                        message = EmptyValues.EMPTY_STRING,
                        isError = false
                    )
                }
            }

            is RegistrationEvents.Login -> login(event.email, event.password)
            is RegistrationEvents.ClearUiEvent -> clearUiEvent()
            is RegistrationEvents.ResetPassword -> {
                sendEmailToResetPassword(event.email)
            }

            is RegistrationEvents.SignUp -> signUp(event.email, event.password, event.username)
            else -> {}
        }


    }

    private fun login(email: String, password: String) {
        scope.launch {
            registrationUseCases.logIn(email, password) { success, uid ->
                if (success) {
                    WebStorageHelper.saveTokenExpiry()
                    navigateToTutorScreen()

                } else {
                    stateForErrorMessage()
                }
            }
        }
    }

    private fun navigateToTutorScreen() {
        _state.update {
            it.copy(
                uiEvent = RegistrationUiEvents.NavigateToTutorFlow
            )
        }
    }

    private fun clearUiEvent() {
        _state.update { it.copy(uiEvent = RegistrationUiEvents.None) }
    }

    private fun sendEmailToResetPassword(email: String) {
        scope.launch {
            registrationUseCases.sendEmailForChangePassword(email = email) { success, uid ->
                if (success) {
                    _state.update {
                        it.copy(
                            showCustomMessage = true,
                            message = success_message_reset_password,
                            isError = false
                        )
                    }
                } else {
                    stateForErrorMessage(message = error_message_reset_password)
                }
            }
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

    private fun signUp(email: String, password: String, username: String) {
        scope.launch {
            registrationUseCases.signUp(
                email = email,
                password = password,
                username = username
            ) { success, uid ->

                if (success) {
                    WebStorageHelper.saveTokenExpiry()
                    navigateToTutorScreen()
                } else {
                    stateForErrorMessage()
                }
            }
        }
    }
}
