package org.example.project.registration.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.example.project.components.customToast.CustomToastComposable
import org.example.project.registration.login.LoginComposable
import org.example.project.registration.signUp.SignupComposable
import org.example.project.registration.viewmodelAndState.state.RegistrationState
import org.example.project.registration.viewmodelAndState.state.RegistrationUiEvents

@Composable
fun RegistrationComposable(
    state: RegistrationState,
    onEvent: (RegistrationUiEvents) -> Unit,
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf(RegistrationScreens.Login, RegistrationScreens.SignUp)
    var showErrorMessage by remember(state.showCustomMessage) { mutableStateOf(state.showCustomMessage) }
    var errorMessage by remember(state.message) { mutableStateOf(state.message) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    text = { Text(title.name) },
                    selected = selectedTab == index,
                    onClick = { selectedTab = index }
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        when (selectedTab) {
            0 -> LoginComposable { email, password ->
                onEvent(RegistrationUiEvents.Login(email, password))
            }

            1 -> SignupComposable { email, password ->
                onEvent(RegistrationUiEvents.SignUp(email, password))
            }
        }

        if (showErrorMessage && errorMessage.isNotEmpty()) {
            CustomToastComposable(
                isError = true,
                message = errorMessage,
                initializeMessage = { showErrorMessage = true }
            )
            LaunchedEffect(state.message) {
                delay(3000L)
                onEvent(RegistrationUiEvents.InitializeStateAfterShowingMessage)
            }
        }
    }

}

enum class RegistrationScreens {
    Login,
    SignUp
}