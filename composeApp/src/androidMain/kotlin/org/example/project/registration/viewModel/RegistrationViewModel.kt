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

    fun onEvent(event: RegistrationUiEvents) {
        when (event) {
            is RegistrationUiEvents.Login -> {
                login(event.email, event.password)
            }

            RegistrationUiEvents.InitializeStateAfterShowingMessage -> {
                _state.update {
                    it.copy(
                        showCustomMessage = false,
                        message = EmptyValues.EMPTY_STRING,
                        isError = false
                    )
                }
            }

            is RegistrationUiEvents.SignUp -> {
                sinUp(event.email, event.password)
            }

            is RegistrationUiEvents.ResetPassword -> {
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

    private fun sinUp(email: String, password: String) {
        viewModelScope.launch(dispatchersIo) {
            flow {
                emit(
                    registrationUseCase.signUp(
                        email = email, password = password
                    ) { success, userId ->
                        Log.d(
                            "Q12345 ", "userId $userId " +
                                    " success $success "
                        )
                        if (success) {
                            //todo
                        } else {
                            stateForErrorMessage()
                        }
                    }
                )
            }.catch {
                Log.d("Q12345 ", "catch $it")
                _state.update {
                    it.copy(
                        uiEvent = RegistrationUiEvents.None,
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
                            //todo
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