package org.example.project.settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import org.example.project.settings.screens.SettingsFragment
import org.example.project.utils.Utils.loadFragmentContainer

class SettingsActivity : FragmentActivity() {
    companion object {
        fun newIntent(context: Context): Intent =
            Intent(context, SettingsActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadFragmentContainer(SettingsFragment.newInstance(), savedInstanceState)
    }
}
