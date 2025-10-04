package org.example.project.registration.viewModel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.example.project.registration.repo.WebAuthRepositoryImpl
import org.example.project.registration.state.RegistrationState
//TODO ELPIDA
class WebRegistrationController(
    private val authRepository: WebAuthRepositoryImpl
) {
    private val scope = CoroutineScope(Dispatchers.Default)

    private val _state = MutableStateFlow(RegistrationState())
    val state: StateFlow<RegistrationState> = _state

    fun login(email: String, password: String) {
        scope.launch {
            authRepository.login(email, password) { success, uid ->
                _state.value = if (success) {
                    RegistrationState(
                        showCustomMessage = true,
                        message = "Welcome $uid"
                    )
                } else {
                    RegistrationState(
                        showCustomMessage = true,
                        message = "Login failed"
                    )
                }
            }
        }
    }

    fun signUp(email: String, password: String) {
        scope.launch {
            authRepository.signup(email, password) { success, uid ->
                _state.value = if (success) {
                    RegistrationState(
                        showCustomMessage = true,
                        message = "Signed up $uid"
                    )
                } else {
                    RegistrationState(
                        showCustomMessage = true,
                        message = "Signup failed"
                    )
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
