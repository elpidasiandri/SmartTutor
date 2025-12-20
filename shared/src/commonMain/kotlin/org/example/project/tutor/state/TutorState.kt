package org.example.project.tutor.state

import org.example.project.utils.EmptyValues

data class TutorState(
    val event: TutorEvents = TutorEvents.None,
    val uiEvent: TutorUiEvents = TutorUiEvents.None,
    val email: String = EmptyValues.EMPTY_STRING,
    val message: String = EmptyValues.EMPTY_STRING,
    val isError: Boolean = EmptyValues.EMPTY_BOOLEAN,
    val showCustomMessage: Boolean = EmptyValues.EMPTY_BOOLEAN,
)

sealed class TutorUiEvents {
    data object None : TutorUiEvents()
    data object Logout : TutorUiEvents()
    data object InitializeStateAfterShowingMessage : TutorUiEvents()
    data object ClearUiEvent : TutorUiEvents()
    data class ShowErrorMessage(val message: String) : TutorUiEvents()
}

sealed class TutorEvents {
    data object None : TutorEvents()
    data object NavigateToLogOut : TutorEvents()
}