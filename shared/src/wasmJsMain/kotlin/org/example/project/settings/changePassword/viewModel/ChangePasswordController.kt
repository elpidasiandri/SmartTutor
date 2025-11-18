package org.example.project.settings.changePassword.viewModel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.project.settings.changePassword.state.ChangePasswordEvents
import org.example.project.settings.changePassword.state.ChangePasswordState
import org.example.project.settings.changePassword.state.ChangePasswordUiEvents
import org.example.project.settings.changePassword.useCase.ChangePasswordUseCase
import org.example.project.strings.SmartTutorStrings
import org.example.project.utils.EmptyValues

class ChangePasswordController(
    private val changePasswordUser: ChangePasswordUseCase,
) {
    private val scope = CoroutineScope(Dispatchers.Default)
    private val _state = MutableStateFlow(ChangePasswordState())
    val state: StateFlow<ChangePasswordState> = _state

    fun onEvent(event: ChangePasswordUiEvents) {
        when (event) {
            is ChangePasswordUiEvents.ChangePassword -> {
                changePassword(
                    oldPassword = event.oldPassword,
                    newPassword = event.newPassword
                )
            }

            ChangePasswordUiEvents.InitializeStateAfterShowingMessage -> {
                _state.update {
                    it.copy(
                        showCustomMessage = false,
                        message = EmptyValues.EMPTY_STRING,
                        isError = false
                    )
                }
            }

            ChangePasswordUiEvents.Initialize -> {
                _state.update {
                    it.copy(
                        event = ChangePasswordEvents.None,
                        uiEvent = ChangePasswordUiEvents.None,
                        isLoading = false
                    )
                }
            }

            ChangePasswordUiEvents.NavigateBack -> {
                _state.update {
                    it.copy(
                        event = ChangePasswordEvents.NavigateBack
                    )
                }
            }

            else -> {

            }
        }
    }

    private fun changePassword(oldPassword: String, newPassword: String) {
        scope.launch {
            _state.update {
                it.copy(
                    isLoading = true
                )
            }
            flow {
                emit(
                    changePasswordUser(
                        oldPassword = oldPassword,
                        newPassword = newPassword,
                        onResult = { success, msg ->
                            if (success) {
                                _state.update {
                                    it.copy(
                                        event = ChangePasswordEvents.Result
                                    )
                                }
                            } else {
                                stateForErrorMessage()
                            }
                        }
                    )
                )
            }.catch {
                stateForErrorMessage()
            }.collect {
            }
        }
    }

    private fun stateForErrorMessage(message: String? = null) {
        _state.update {
            it.copy(
                showCustomMessage = true,
                message = message ?: SmartTutorStrings.generic_error,
                isError = true,
                isLoading = false
            )
        }
    }
}