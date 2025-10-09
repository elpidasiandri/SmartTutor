package org.example.project.registration.viewModel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.project.extensions.logD
import org.example.project.registration.repo.WebAuthRepositoryImpl
import org.example.project.registration.state.RegistrationState
import org.example.project.strings.SmartTutorStrings

class WebRegistrationController(
    private val authRepository: WebAuthRepositoryImpl,
) {
    private val scope = CoroutineScope(Dispatchers.Default)
    private val _state = MutableStateFlow(RegistrationState())
    val state: StateFlow<RegistrationState> = _state

    fun login(email: String, password: String) {
        scope.launch {
            logD("Q12345 login")

            authRepository.login(email, password) { success, uid ->
                logD("Q12345 success $success")

                if (success) {
                    //TODO

                    _state.update {
                        it.copy(
                            showCustomMessage = true,
                            message = "Welcome $uid",
                            isError = false
                        )
                    }
                } else {
                    stateForErrorMessage()
                }
            }
        }
    }

    private fun stateForErrorMessage() {
        logD("Q12345 stateForErrorMessage")
        _state.update {
            it.copy(
                showCustomMessage = true,
                message = SmartTutorStrings.generic_error,
                isError = true
            )
        }
    }

    fun signUp(email: String, password: String) {
        scope.launch {
            authRepository.signup(email, password) { success, uid ->

                if (success) {
                    _state.update {
                        it.copy(
                            showCustomMessage = true,
                            message = "Signed up $uid"
                        )
                    }
                } else {
                    stateForErrorMessage()
                }
            }
        }
    }

    fun dismissMessage() {
        _state.value = _state.value.copy(
            showCustomMessage = false,
            message = ""
        )
    }
}
