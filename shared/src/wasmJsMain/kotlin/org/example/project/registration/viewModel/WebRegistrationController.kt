package org.example.project.registration.viewModel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.project.extensions.logD
import org.example.project.registration.state.RegistrationState
import org.example.project.registration.state.RegistrationUiEvents
import org.example.project.registration.useCases.registration.RegistrationUseCase
import org.example.project.storage.WebStorageHelper
import org.example.project.strings.SmartTutorStrings
import org.example.project.strings.SmartTutorStrings.error_message_reset_password
import org.example.project.strings.SmartTutorStrings.success_message_reset_password

class WebRegistrationController(
    private val registrationUseCases: RegistrationUseCase,
) {
    private val scope = CoroutineScope(Dispatchers.Default)
    private val _state = MutableStateFlow(RegistrationState())
    val state: StateFlow<RegistrationState> = _state

    fun login(email: String, password: String) {
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
    fun clearUiEvent() {
        _state.update { it.copy(uiEvent = RegistrationUiEvents.None) }
    }
    fun sendEmailToResetPassword(email: String) {
        scope.launch {
            logD("Q12345 sendEmailToResetPassword")

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

    fun signUp(email: String, password: String, username: String) {
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

    fun dismissMessage() {
        _state.value = _state.value.copy(
            showCustomMessage = false,
            message = ""
        )
    }
}
