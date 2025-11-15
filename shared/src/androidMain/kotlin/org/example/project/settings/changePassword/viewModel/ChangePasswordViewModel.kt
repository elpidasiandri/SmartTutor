package org.example.project.settings.changePassword.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
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

class ChangePasswordViewModel(
    private val dispatchersIo: CoroutineDispatcher,
    private val changePasswordUseCase: ChangePasswordUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(ChangePasswordState())
    val state: StateFlow<ChangePasswordState> = _state

    fun onEvent(event: ChangePasswordUiEvents) {
        when (event) {
            ChangePasswordUiEvents.InitializeStateAfterShowingMessage -> {
                _state.update {
                    it.copy(
                        showCustomMessage = false,
                        message = EmptyValues.EMPTY_STRING,
                        isError = false
                    )
                }
            }

            is ChangePasswordUiEvents.ChangePassword -> {
                changePassword(
                    oldPassword = event.oldPassword,
                    newPassword = event.newPassword
                )
            }

            else -> {}
        }
    }

    fun setEventNone() {
        _state.update {
            it.copy(
                event = ChangePasswordEvents.None,
                uiEvent = ChangePasswordUiEvents.None
            )
        }
    }

    private fun changePassword(oldPassword: String, newPassword: String) {
        viewModelScope.launch(dispatchersIo) {
            _state.update {
                it.copy(
                    isLoading = true
                )
            }
            flow {
                emit(
                    changePasswordUseCase(
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