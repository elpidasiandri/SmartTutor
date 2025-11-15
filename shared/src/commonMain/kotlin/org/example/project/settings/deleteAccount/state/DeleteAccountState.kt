package org.example.project.settings.deleteAccount.state

import org.example.project.utils.EmptyValues

data class DeleteAccountState(
    val event: DeleteAccountEvents = DeleteAccountEvents.None,
    val uiEvent: DeleteAccountUiEvents = DeleteAccountUiEvents.None,
    val message: String = EmptyValues.EMPTY_STRING,
    val isError: Boolean = EmptyValues.EMPTY_BOOLEAN,
    val isLoading: Boolean = EmptyValues.EMPTY_BOOLEAN,
    val showCustomMessage: Boolean = EmptyValues.EMPTY_BOOLEAN,
)

sealed class DeleteAccountEvents {
    data object None : DeleteAccountEvents()
    data object AccountDeleted : DeleteAccountEvents()
}
sealed class DeleteAccountUiEvents {
    data object None : DeleteAccountUiEvents()
    data object DeleteAccount : DeleteAccountUiEvents()
    data object InitializeStateAfterShowingMessage : DeleteAccountUiEvents()
}