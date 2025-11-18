package org.example.project.settings

data class SettingsState(
    val event: SettingsEvents = SettingsEvents.None,
    val eventUi: SettingsUiEvents = SettingsUiEvents.None,
)

sealed class SettingsEvents {
    data object None : SettingsEvents()
    data object NavigateToDeleteAccount : SettingsEvents()
    data object Initialize : SettingsEvents()
    data object LeaveSettingsFlow : SettingsEvents()
    data object NavigateToChangePassword : SettingsEvents()
}
sealed class SettingsUiEvents {
    data object None : SettingsUiEvents()
    data object LeaveSettingsFlow : SettingsUiEvents()
    data object NavigateToDeleteAccount : SettingsUiEvents()
    data object NavigateToChangePassword : SettingsUiEvents()
}