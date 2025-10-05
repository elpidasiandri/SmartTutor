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
import org.example.project.registration.useCases.LoginUseCase
import org.example.project.registration.useCases.LogoutUseCase
import org.example.project.registration.useCases.SignUpUseCase
import org.example.project.strings.SmartTutorStrings
import org.example.project.utils.EmptyValues
import org.example.project.registration.state.RegistrationState
import org.example.project.registration.state.RegistrationUiEvents

class RegistrationViewModel(
    private val dispatchersIo: CoroutineDispatcher,
    private val loginUsecase: LoginUseCase,
    private val signUpUsecase: SignUpUseCase,
    private val logoutUsecase: LogoutUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(RegistrationState())
    val state: StateFlow<RegistrationState> = _state

    fun onEvent(event: RegistrationUiEvents) {
        when (event) {
            is RegistrationUiEvents.Login -> login(event.email, event.password)
            RegistrationUiEvents.InitializeStateAfterShowingMessage -> {
                _state.update {
                    it.copy(
                        showCustomMessage = false,
                        message = EmptyValues.EMPTY_STRING
                    )
                }
            }

            is RegistrationUiEvents.SignUp -> sinUp(event.email, event.password)
            else -> {}
        }
    }

    private fun sinUp(email: String, password: String) {
        viewModelScope.launch(dispatchersIo) {
            flow {
                emit(
                    signUpUsecase(
                        email = email, password = password
                    ) { success, userId ->
                        Log.d("Q12345 ", "userId $userId " +
                                " success $success ")
                        if (success) {
                            //todo
                        }else{
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
                    loginUsecase(email = email, password = password) { success, userId ->
                        if (success) {
                            //todo
                        } else {
                            stateForErrorMessage()
                        }
                    })
            }.collect {
               //todo
            }
        }
    }


    private fun stateForErrorMessage() {
        _state.update {
            it.copy(
                showCustomMessage = true,
                message = SmartTutorStrings.generic_error
            )
        }
    }
}