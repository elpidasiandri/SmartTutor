package org.example.project.registrationScreen

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import org.example.project.registration.di.registrationModule
import org.example.project.registration.RegistrationComposable
import org.example.project.registration.state.RegistrationEvents
import org.example.project.registration.state.RegistrationUiEvents
import org.example.project.registration.viewModel.RegistrationViewModel
import org.example.project.tutorScreen.TutorActivity
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.context.loadKoinModules

class LoginSignUpActivity : ComponentActivity() {
    private val registrationViewModel: RegistrationViewModel by viewModel()
    private val tutorLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadKoinModules(registrationModule)
        setUpEventsViewModel()
        setContent {
            val state by registrationViewModel.state.collectAsStateWithLifecycle()
            var showErrorMessage by remember(state.showCustomMessage) { mutableStateOf(state.showCustomMessage) }
            var errorMessage by remember(state.message) { mutableStateOf(state.message) }
            var isError by remember(state.isError) { mutableStateOf(state.isError) }

            RegistrationComposable(
                showCustomMessage = showErrorMessage,
                message = errorMessage,
                isError = isError,
                onLogin = { email, password ->
                    registrationViewModel.onEvent(RegistrationEvents.Login(email, password))
                },
                onSignUp = { email, password, username ->
                    registrationViewModel.onEvent(
                        RegistrationEvents.SignUp(
                            email,
                            password,
                            username
                        )
                    )
                },
                onMessageDismiss = {
                    registrationViewModel.onEvent(RegistrationEvents.InitializeStateAfterShowingMessage)
                },
                resetPassword = { email ->
                    registrationViewModel.onEvent(
                        RegistrationEvents.ResetPassword(
                            email
                        )
                    )
                }
            )
        }
    }

    private fun setUpEventsViewModel() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                registrationViewModel.state.collect { state ->
                    when (state.uiEvent) {
                        RegistrationUiEvents.NavigateToTutorFlow -> {
                            navigateToTutorFlow()
                        }
                        else -> {}
                    }
                }
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
    }

    private fun navigateToTutorFlow() {
        val intent = Intent(this, TutorActivity::class.java)
        tutorLauncher.launch(intent)
        finish()
    }

    companion object {
        fun newInstance(context: Context) {
            val intent = Intent(context, LoginSignUpActivity::class.java)
            context.startActivity(intent)
        }
    }
}