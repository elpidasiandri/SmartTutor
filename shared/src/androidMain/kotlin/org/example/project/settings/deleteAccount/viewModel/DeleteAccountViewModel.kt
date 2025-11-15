package org.example.project.settings.deleteAccount.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.project.settings.deleteAccount.state.DeleteAccountState
import org.example.project.settings.deleteAccount.state.DeleteAccountEvents
import org.example.project.settings.deleteAccount.state.DeleteAccountUiEvents
import org.example.project.settings.deleteAccount.useCase.DeleteAccountUseCase
import org.example.project.settings.logout.useCase.LogoutUseCase
import org.example.project.strings.SmartTutorStrings
import org.example.project.utils.EmptyValues

class DeleteAccountViewModel(
    private val dispatchersIo: CoroutineDispatcher,
    private val deleteAccount: DeleteAccountUseCase,
    private val logout: LogoutUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(DeleteAccountState())
    val state: StateFlow<DeleteAccountState> = _state

    fun onEvent(event: DeleteAccountUiEvents) {
        when (event) {
            DeleteAccountUiEvents.DeleteAccount -> deleteUserAccount()
            DeleteAccountUiEvents.InitializeStateAfterShowingMessage -> {
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

    fun deleteUserAccount() {
        viewModelScope.launch(dispatchersIo) {
            _state.update {
                it.copy(
                    isLoading = true
                )
            }
            deleteAccount { success, msg ->
                if (success) {
                    logout { success, msg ->
                        if (success) {
                            _state.update {
                                it.copy(
                                    event = DeleteAccountEvents.AccountDeleted,
                                    isLoading = false
                                )
                            }
                        } else {
                            stateForErrorMessage()
                        }
                    }

                } else {
                    stateForErrorMessage()
                }
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