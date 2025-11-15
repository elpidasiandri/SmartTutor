package org.example.project.tutor.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.project.settings.logout.useCase.LogoutUseCase
import org.example.project.strings.SmartTutorStrings
import org.example.project.tutor.TutorEvents
import org.example.project.tutor.TutorState
import org.example.project.tutor.TutorUiEvents
import org.example.project.utils.EmptyValues

class TutorIntroViewModel(
    private val dispatchersIo: CoroutineDispatcher,
    private val logout: LogoutUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(TutorState())
    val state: StateFlow<TutorState> = _state


    fun onEvent(event: TutorUiEvents) {
        when (event) {
            TutorUiEvents.Logout -> {
                logOutUser()
            }
            TutorUiEvents.InitializeStateAfterShowingMessage ->{
                _state.update {
                    it.copy(
                        showCustomMessage = false,
                        message = EmptyValues.EMPTY_STRING,
                        isError = false
                    )
                }
            }
            else -> {}
        }
    }

    private fun logOutUser() {
        println("Q12345 logOutUser")
        viewModelScope.launch(dispatchersIo) {
            flow {
                emit(
                    logout { success, _ ->
                        println("Q12345 success $success")
                        if (success) {
                            println("success true")
                            _state.update {
                                it.copy(
                                    event = TutorEvents.NavigateToLogOut
                                )
                            }
                        } else {
                            stateForErrorMessage()
                        }
                    }
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
                isError = true
            )
        }
    }
}