package org.example.project.registration

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.example.project.components.customToast.CustomToastComposable
import org.example.project.dimens.Dimens.spacing16
import org.example.project.dimens.Dimens.spacing24
import org.example.project.registration.login.LoginComposable
import org.example.project.registration.reset.ResetPasswordComposable
import org.example.project.registration.signUp.SignupComposable

@Composable
fun RegistrationComposable(
    showCustomMessage: Boolean,
    message: String,
    isError: Boolean,
    onLogin: (email: String, password: String) -> Unit,
    onSignUp: (email: String, password: String) -> Unit,
    onMessageDismiss: () -> Unit,
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf(RegistrationScreens.Login, RegistrationScreens.SignUp)
    var showErrorMessage by remember(showCustomMessage) { mutableStateOf(showCustomMessage) }
    var errorMessage by remember(message) { mutableStateOf(message) }

    if (showErrorMessage && errorMessage.isNotEmpty()) {
        Box(modifier = Modifier.padding(spacing24)) {
            CustomToastComposable(
                isError = isError,
                message = errorMessage,
                initializeMessage = { showErrorMessage = true }
            )
        }

        LaunchedEffect(errorMessage) {
            delay(3000L)
            onMessageDismiss()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(spacing16),
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
            0 -> {
                var showReset by remember { mutableStateOf(false) }
                if (showReset) {
                    ResetPasswordComposable(
                        onBackToLogin = { showReset = false },
                        resetPassword = {})
                } else {
                    LoginComposable(
                        login = { email, password ->
                            onLogin(email, password)
                        },
                        onForgotPasswordClick = {
                            showReset = true
                        })
                }
            }

            1 -> SignupComposable { email, password ->
                onSignUp(email, password)
            }
        }
    }
}

enum class RegistrationScreens {
    Login,
    SignUp
}