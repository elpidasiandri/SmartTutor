package org.example.project.settings.changePassword.state

import org.example.project.utils.EmptyValues

data class ChangePasswordState(
    val event: ChangePasswordEvents = ChangePasswordEvents.None,
    val uiEvent: ChangePasswordUiEvents = ChangePasswordUiEvents.None,
    val message: String = EmptyValues.EMPTY_STRING,
    val isError: Boolean = EmptyValues.EMPTY_BOOLEAN,
    val isLoading: Boolean = EmptyValues.EMPTY_BOOLEAN,
    val showCustomMessage: Boolean = EmptyValues.EMPTY_BOOLEAN,
)
sealed class ChangePasswordEvents {
    data object None : ChangePasswordEvents()
    data object Result : ChangePasswordEvents()
}
sealed class ChangePasswordUiEvents {
    data object None : ChangePasswordUiEvents()
    data object InitializeStateAfterShowingMessage : ChangePasswordUiEvents()
    data class ChangePassword(val oldPassword: String, val newPassword:String) : ChangePasswordUiEvents()
}