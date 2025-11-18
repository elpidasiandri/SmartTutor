package org.example.project.settings.deleteAccount.viewModel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.project.settings.deleteAccount.state.DeleteAccountEvents
import org.example.project.settings.deleteAccount.state.DeleteAccountState
import org.example.project.settings.deleteAccount.state.DeleteAccountUiEvents
import org.example.project.settings.deleteAccount.useCase.DeleteAccountUseCase
import org.example.project.settings.logout.useCase.LogoutUseCase
import org.example.project.strings.SmartTutorStrings
import org.example.project.utils.EmptyValues

class DeleteAccountController(
    private val deleteAccount: DeleteAccountUseCase,
    private val logOut: LogoutUseCase,
) {
    private val _state = MutableStateFlow(DeleteAccountState())
    val state: StateFlow<DeleteAccountState> = _state
    private val scope = CoroutineScope(Dispatchers.Default)


    fun onEvent(event: DeleteAccountUiEvents) {
        when (event) {
            DeleteAccountUiEvents.DeleteAccount -> {
                scope.launch {
                    _state.update {
                        it.copy(
                            isLoading = true
                        )
                    }
                    deleteAccount { success, msg ->
                        if (success) {
                            logOut { success, msg ->
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

            DeleteAccountUiEvents.InitializeStateAfterShowingMessage -> {
                _state.update {
                    it.copy(
                        showCustomMessage = false,
                        message = EmptyValues.EMPTY_STRING,
                        isError = false
                    )
                }
            }

            else -> {

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