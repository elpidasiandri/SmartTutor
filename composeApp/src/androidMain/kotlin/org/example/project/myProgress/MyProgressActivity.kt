package org.example.project.myProgress

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.fragment.app.FragmentActivity
import org.example.project.myProgress.module.myProgressModule
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
import kotlin.getValue

class MyProgressActivity : FragmentActivity() {
    private val viewModel: MyProgressViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadKoinModules(myProgressModule)
        setContent {
            val state by viewModel.state.collectAsState()

            MyProgressScreen(
                state = state,
                onBackClick = { finish() }
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unloadKoinModules(myProgressModule)
    }

    companion object {
        fun newInstance(context: Context) {
            val intent = Intent(context, MyProgressActivity::class.java)
            context.startActivity(intent)
        }
    }
}