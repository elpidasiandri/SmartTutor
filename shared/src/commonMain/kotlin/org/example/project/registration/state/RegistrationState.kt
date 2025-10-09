package org.example.project.registration.state

import org.example.project.utils.EmptyValues

data class RegistrationState(
    val password: String = EmptyValues.EMPTY_STRING,
    val email: String = EmptyValues.EMPTY_STRING,
    val message: String = EmptyValues.EMPTY_STRING,
    val isError: Boolean = EmptyValues.EMPTY_BOOLEAN,
    val showCustomMessage: Boolean = EmptyValues.EMPTY_BOOLEAN,
    val uiEvent: RegistrationUiEvents = RegistrationUiEvents.None,
)

sealed class RegistrationUiEvents {
    data object None : RegistrationUiEvents()
    data object InitializeStateAfterShowingMessage : RegistrationUiEvents()
    data class SignUp(val email: String, val password: String) : RegistrationUiEvents()
    data class Login(val email: String, val password: String) : RegistrationUiEvents()
    data class ResetPassword(val email: String) : RegistrationUiEvents()

}
