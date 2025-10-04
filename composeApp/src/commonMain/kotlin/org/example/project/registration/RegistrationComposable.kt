package org.example.project.registration

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
@Composable
fun RegistrationComposable(
    showCustomMessage: Boolean,
    message: String,
    onLogin: (email: String, password: String) -> Unit,
    onSignUp: (email: String, password: String) -> Unit,
    onMessageDismiss: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf(RegistrationScreens.Login, RegistrationScreens.SignUp)
    var showErrorMessage by remember(showCustomMessage) { mutableStateOf(showCustomMessage) }
    var errorMessage by remember(message) { mutableStateOf(message) }

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
                onLogin(email, password)
            }

            1 -> SignupComposable { email, password ->
                onSignUp(email, password)
            }
        }

        if (showErrorMessage && errorMessage.isNotEmpty()) {
            CustomToastComposable(
                isError = true,
                message = errorMessage,
                initializeMessage = { showErrorMessage = true }
            )
            LaunchedEffect(errorMessage) {
                delay(3000L)
                onMessageDismiss()
            }
        }
    }

}

enum class RegistrationScreens {
    Login,
    SignUp
}