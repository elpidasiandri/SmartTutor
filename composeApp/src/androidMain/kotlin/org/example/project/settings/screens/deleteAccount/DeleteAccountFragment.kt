package org.example.project.settings.screens.deleteAccount

import android.content.Intent
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
import kotlinx.coroutines.launch
import org.example.project.registrationScreen.RegistrationActivity
import org.example.project.settings.deleteAccount.DeleteAccountScreen
import org.example.project.settings.deleteAccount.di.deleteAccountModule
import org.example.project.settings.deleteAccount.state.DeleteAccountEvents
import org.example.project.settings.deleteAccount.state.DeleteAccountUiEvents
import org.example.project.settings.deleteAccount.viewModel.DeleteAccountViewModel
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules

class DeleteAccountFragment : Fragment() {
    private val viewModel by activityViewModel<DeleteAccountViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        loadKoinModules(deleteAccountModule)
        setUpObserver()
        return ComposeView(requireContext()).apply {
            setContent {
                val state by viewModel.state.collectAsStateWithLifecycle()
                var showErrorMessage by remember(state.showCustomMessage) { mutableStateOf(state.showCustomMessage) }
                var errorMessage by remember(state.message) { mutableStateOf(state.message) }
                var isError by remember(state.isError) { mutableStateOf(state.isError) }
                var isLoading by remember(state.isLoading) { mutableStateOf(state.isLoading) }

                DeleteAccountScreen(
                    isLoading = isLoading,
                    showCustomMessage = showErrorMessage,
                    message = errorMessage,
                    isError = isError,
                    onBack = { parentFragmentManager.popBackStack() },
                    onDeleteConfirmed = {
                        viewModel.onEvent(DeleteAccountUiEvents.DeleteAccount)
                    },
                    onCancel = { parentFragmentManager.popBackStack() },
                    onMessageDismiss = {
                        viewModel.onEvent(DeleteAccountUiEvents.InitializeStateAfterShowingMessage)
                    }
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unloadKoinModules(deleteAccountModule)
    }

    private fun setUpObserver() {
        viewLifecycleOwner.lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { event ->
                    when (event.event) {
                        DeleteAccountEvents.AccountDeleted -> {
                            startActivity(
                                Intent(
                                    requireContext(),
                                    RegistrationActivity::class.java
                                )
                            )
                            requireActivity().finishAffinity()
                        }

                        else -> {}
                    }
                }
            }
        }
    }
}