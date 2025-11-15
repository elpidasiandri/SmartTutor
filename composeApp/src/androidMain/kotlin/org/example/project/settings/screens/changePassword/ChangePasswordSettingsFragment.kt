package org.example.project.settings.screens.changePassword

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.example.project.settings.changePassword.ChangePasswordSettingsScreen
import org.example.project.settings.changePassword.di.changePasswordModule
import org.example.project.settings.changePassword.state.ChangePasswordEvents
import org.example.project.settings.changePassword.state.ChangePasswordUiEvents
import org.example.project.settings.changePassword.viewModel.ChangePasswordViewModel
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules

class ChangePasswordSettingsFragment : Fragment() {
    private val viewModel by activityViewModel<ChangePasswordViewModel>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        loadKoinModules(changePasswordModule)
        setUpObserver()
        return ComposeView(requireContext()).apply {
            setContent {
                val state by viewModel.state.collectAsStateWithLifecycle()
                var showErrorMessage by remember(state.showCustomMessage) { mutableStateOf(state.showCustomMessage) }
                var errorMessage by remember(state.message) { mutableStateOf(state.message) }
                var isError by remember(state.isError) { mutableStateOf(state.isError) }

                ChangePasswordSettingsScreen(
                    showCustomMessage = showErrorMessage,
                    message = errorMessage,
                    isError = isError,
                    isLoading = state.isLoading,
                    onBack = { parentFragmentManager.popBackStack() },
                    changePassword = { old, new ->
                        viewModel.onEvent(
                            ChangePasswordUiEvents.ChangePassword(
                                oldPassword = old,
                                newPassword = new
                            )
                        )
                    },
                    onMessageDismiss = {
                        viewModel.onEvent(ChangePasswordUiEvents.InitializeStateAfterShowingMessage)
                    }
                )
            }
        }
    }

    private fun setUpObserver() {
        viewLifecycleOwner.lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { event ->
                    when (event.event) {
                        ChangePasswordEvents.Result -> {
                            delay(2000)
                            viewModel.setEventNone()
                            parentFragmentManager.popBackStack()
                        }

                        else -> {}
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unloadKoinModules(changePasswordModule)
    }
}