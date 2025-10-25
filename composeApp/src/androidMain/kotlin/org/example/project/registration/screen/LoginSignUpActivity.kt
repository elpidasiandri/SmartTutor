package org.example.project.registration.screen

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.example.project.registration.di.registrationModule
import org.example.project.registration.RegistrationComposable
import org.example.project.registration.state.RegistrationUiEvents
import org.example.project.registration.viewModel.RegistrationViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.context.loadKoinModules

class LoginSignUpActivity : ComponentActivity() {
    private val registrationViewModel: RegistrationViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadKoinModules(registrationModule)
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
                    registrationViewModel.onEvent(RegistrationUiEvents.Login(email, password))
                },
                onSignUp = { email, password, username ->
                    registrationViewModel.onEvent(
                        RegistrationUiEvents.SignUp(
                            email,
                            password,
                            username
                        )
                    )
                },
                onMessageDismiss = {
                    registrationViewModel.onEvent(RegistrationUiEvents.InitializeStateAfterShowingMessage)
                },
                resetPassword = { email ->
                    registrationViewModel.onEvent(
                        RegistrationUiEvents.ResetPassword(
                            email
                        )
                    )
                }
            )
        }
    }


    override fun onDestroy() {
        super.onDestroy()
    }

    companion object {
        fun newInstance(context: Context) {
            val intent = Intent(context, LoginSignUpActivity::class.java)
            context.startActivity(intent)
        }
    }
}