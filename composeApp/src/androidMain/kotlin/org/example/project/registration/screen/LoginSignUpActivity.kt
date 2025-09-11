package org.example.project.registration.screen

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.example.project.registration.di.registrationModule
import org.example.project.registration.ui.RegistrationComposable
import org.example.project.registration.viewmodelAndState.viewModel.RegistrationViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.context.loadKoinModules

class LoginSignUpActivity : ComponentActivity() {
    private val registrationViewModel: RegistrationViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadKoinModules(registrationModule)
        setContent {
            val state by registrationViewModel.state.collectAsStateWithLifecycle()
            RegistrationComposable(
                state = state,
                onEvent = registrationViewModel::onEvent
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