package org.example.project.navigations.myprogress

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.example.project.myProgress.MyProgressController
import org.example.project.myProgress.MyProgressScreen
import org.example.project.myProgress.di.myProgressModule
import org.example.project.navController.NavController
import org.example.project.navController.WebScreen
import org.koin.compose.koinInject
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules

@Composable
fun MyProgressFlow (navController: NavController) {
    loadKoinModules(myProgressModule)
    val controller = koinInject<MyProgressController>()
    val state by controller.state.collectAsState()

    MyProgressScreen(
        state = state,
        onBackClick = {
            unloadKoinModules(myProgressModule)
            navController.navigate(WebScreen.Tutor)
        }
    )
}