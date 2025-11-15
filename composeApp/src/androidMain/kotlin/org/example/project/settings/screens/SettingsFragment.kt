package org.example.project.settings.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import org.example.project.settings.SettingsScreen
import org.example.project.settings.screens.changePassword.ChangePasswordSettingsFragment
import org.example.project.settings.screens.deleteAccount.DeleteAccountFragment
import org.example.project.utils.Utils.navigateTo

class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                SettingsScreen(
                    onChangePasswordClick = {
                        navigateTo(ChangePasswordSettingsFragment())
                    },
                    onDeleteAccountClick = {
                        navigateTo(DeleteAccountFragment())
                    },
                    onBack = {
                        activity?.finish()
                    }
                )
            }
        }
    }
    companion object {
        fun newInstance(): SettingsFragment {
            return SettingsFragment().apply {
                arguments = Bundle().apply {}
            }
        }
    }
}
