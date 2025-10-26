package org.example.project.registration.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.project.strings.SmartTutorStrings
import org.example.project.utils.EmptyValues
import org.example.project.registration.state.RegistrationState
import org.example.project.registration.state.RegistrationEvents
import org.example.project.registration.state.RegistrationUiEvents
import org.example.project.registration.useCases.registration.RegistrationUseCase
import org.example.project.strings.SmartTutorStrings.error_message_reset_password
import org.example.project.strings.SmartTutorStrings.success_message_reset_password

class RegistrationViewModel(
    private val dispatchersIo: CoroutineDispatcher,
    private val registrationUseCase: RegistrationUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(RegistrationState())
    val state: StateFlow<RegistrationState> = _state

    fun onEvent(event: RegistrationEvents) {
        when (event) {
            is RegistrationEvents.Login -> {
                login(event.email, event.password)
            }

            RegistrationEvents.InitializeStateAfterShowingMessage -> {
                _state.update {
                    it.copy(
                        showCustomMessage = false,
                        message = EmptyValues.EMPTY_STRING,
                        isError = false
                    )
                }
            }

            is RegistrationEvents.SignUp -> {
                sinUp(event.email, event.password, event.username)
            }

            is RegistrationEvents.ResetPassword -> {
                changePassword(event.email)
            }

            else -> {}
        }
    }

    private fun changePassword(
        email: String,
    ) {
        viewModelScope.launch(dispatchersIo) {
            flow {
                emit(
                    registrationUseCase.sendEmailForChangePassword(
                        email = email
                    ) { success, userId ->
                        Log.d(
                            "Q12345 ", " registrationUseCase userId $userId " +
                                    " success $success "
                        )
                        if (success) {
                            //todo
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
                )
            }.catch {
                Log.d(
                    "Q12345 ", "catch $it"
                )
                stateForErrorMessage()

            }.collect {
                Log.d(
                    "Q12345 ", "collect"
                )
            }
        }
    }

    private fun sinUp(email: String, password: String, username: String) {
        viewModelScope.launch(dispatchersIo) {
            flow {
                emit(
                    registrationUseCase.signUp(
                        email = email, password = password, username = username
                    ) { success, userId ->
                        Log.d(
                            "Q12345 ", "userId $userId " +
                                    " success $success "
                        )
                        if (success) {
                            _state.update {
                                it.copy(
                                    uiEvent = RegistrationUiEvents.NavigateToTutorFlow
                                )
                            }
                        } else {
                            stateForErrorMessage()
                        }
                    }
                )
            }.catch {
                Log.d("Q12345 ", "catch $it")
                _state.update {
                    it.copy(
                        event = RegistrationEvents.None,
                    )
                }
                stateForErrorMessage()
            }.collect {
                //todo

            }
        }
    }

    private fun login(email: String, password: String) {
        viewModelScope.launch(dispatchersIo) {
            flow {
                emit(
                    registrationUseCase.logIn(
                        email = email,
                        password = password
                    ) { success, userId ->
                        if (success) {
                            _state.update {
                                it.copy(
                                    uiEvent = RegistrationUiEvents.NavigateToTutorFlow
                                )
                            }
                        } else {
                            stateForErrorMessage()
                        }
                    })
            }.catch {
                stateForErrorMessage()
            }.collect {
                //todo
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
}