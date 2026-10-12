
package org.example.project.navigations.myprogress

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import org.example.project.myProgress.MyProgressController
import org.example.project.myProgress.MyProgressScreen
import org.example.project.navController.NavController
import org.example.project.navController.WebScreen
import org.example.project.registration.AuthUserHolder
import org.koin.compose.getKoin
import org.koin.core.parameter.parametersOf
@Composable
fun MyProgressFlow(navController: NavController) {


    val user = AuthUserHolder.currentUser

    if (user == null || !user.emailVerified) {
        LaunchedEffect(Unit) {
            navController.navigate(WebScreen.Registration)
        }
        return
    }

    val userId = user.uid


    val koin = getKoin()

    val controller = remember(userId) {
        koin.get<MyProgressController> {
            parametersOf(userId)
        }
    }
    val state by controller.state.collectAsState()

    MyProgressScreen(
        state = state,
        onBackClick = {
            navController.navigate(WebScreen.Tutor)
        }
    )
}
