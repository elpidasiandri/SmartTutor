package org.example.project.registration.state

import org.example.project.utils.EmptyValues

data class RegistrationState(
    val password: String = EmptyValues.EMPTY_STRING,
    val email: String = EmptyValues.EMPTY_STRING,
    val message: String = EmptyValues.EMPTY_STRING,
    val isError: Boolean = EmptyValues.EMPTY_BOOLEAN,
    val showCustomMessage: Boolean = EmptyValues.EMPTY_BOOLEAN,
    val event: RegistrationEvents = RegistrationEvents.None,
    val uiEvent: RegistrationUiEvents = RegistrationUiEvents.None,
)

sealed class RegistrationUiEvents {
    data object None : RegistrationUiEvents()
    data object NavigateToTutorFlow : RegistrationUiEvents()
}

sealed class RegistrationEvents {
    data object None : RegistrationEvents()
    data object InitializeStateAfterShowingMessage : RegistrationEvents()
    data class SignUp(val email: String, val password: String, val username: String) :
        RegistrationEvents()

    data class Login(val email: String, val password: String) : RegistrationEvents()
    data class ResetPassword(val email: String) : RegistrationEvents()

}
