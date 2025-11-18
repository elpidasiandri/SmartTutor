package org.example.project.settings.settingsView.viewModel

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import org.example.project.settings.SettingsEvents
import org.example.project.settings.SettingsState
import org.example.project.settings.SettingsUiEvents


class SettingsController() {
    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state

    fun onEvent(event: SettingsEvents) {
        when (event) {
            SettingsEvents.Initialize -> {
                _state.update {
                    it.copy(
                        eventUi = SettingsUiEvents.None,
                        event = SettingsEvents.None
                    )
                }
            }

            SettingsEvents.NavigateToChangePassword -> {
                _state.update {
                    it.copy(
                        eventUi = SettingsUiEvents.NavigateToChangePassword,
                    )
                }
            }

            SettingsEvents.LeaveSettingsFlow -> {
                _state.update {
                    it.copy(
                        eventUi = SettingsUiEvents.LeaveSettingsFlow,
                    )
                }
            }

            SettingsEvents.NavigateToDeleteAccount -> {
                _state.update {
                    it.copy(
                        eventUi = SettingsUiEvents.NavigateToDeleteAccount,
                    )
                }
            }

            else -> {}
        }
    }
}