package org.example.project.settings.changePassword

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import org.example.project.components.customToast.CustomToastComposable
import org.example.project.components.errorText.ErrorTextComposable
import org.example.project.components.loading.LoadingScreen
import org.example.project.dimens.Dimens.spacing12
import org.example.project.dimens.Dimens.spacing16
import org.example.project.dimens.Dimens.spacing24
import org.example.project.strings.SmartTutorStrings.change_password_title
import org.example.project.strings.SmartTutorStrings.confirm_password_title
import org.example.project.strings.SmartTutorStrings.invalid_password
import org.example.project.strings.SmartTutorStrings.new_password
import org.example.project.strings.SmartTutorStrings.old_password
import org.example.project.strings.SmartTutorStrings.password_conf_password_not_valid
import org.example.project.strings.SmartTutorStrings.password_dismatch
import org.example.project.strings.SmartTutorStrings.settings_title
import org.example.project.strings.SmartTutorStrings.update_password
import org.example.project.utils.Validation.isValidPassword

@Composable
fun ChangePasswordSettingsScreen(
    showCustomMessage: Boolean,
    message: String,
    isError: Boolean,
    isLoading: Boolean,
    onBack: () -> Unit,
    changePassword: (String, String) -> Unit,
    onMessageDismiss: () -> Unit,
) {
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember(message) { mutableStateOf(message) }
    val isNewPassValid = remember(newPassword) {
        newPassword.length >= 6 && isValidPassword(newPassword)
    }

    val isConfirmPassValid = remember(confirmPassword) {
        confirmPassword.length >= 6 && isValidPassword(confirmPassword)
    }

    val passwordsMatch = remember(newPassword, confirmPassword) {
        newPassword == confirmPassword
    }

    if (isLoading) {
        LoadingScreen()
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(spacing16)
        ) {
            Row {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.padding(end = spacing16)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text(
                    modifier = Modifier.align(Alignment.CenterVertically),
                    text = settings_title,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(Modifier.height(spacing16))

            Text(text = change_password_title, fontSize = 16.sp, fontWeight = FontWeight.Bold)

            Spacer(Modifier.height(spacing12))
            PasswordTextField(
                value = oldPassword,
                onValueChange = { oldPassword = it },
                label = old_password,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(spacing12))

            PasswordTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = new_password,
                modifier = Modifier.fillMaxWidth()
            )

            if (newPassword.length >= 6 && !isNewPassValid) {
                ErrorTextComposable(invalid_password)
            }
            Spacer(Modifier.height(spacing12))
            PasswordTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = confirm_password_title,
                modifier = Modifier.fillMaxWidth()
            )

            if (confirmPassword.length >= 6 && !isConfirmPassValid) {
                ErrorTextComposable(password_conf_password_not_valid)
            } else if (confirmPassword.length >= 6 && !passwordsMatch) {
                ErrorTextComposable(password_dismatch)
            }
            ChangePasswordToast(showCustomMessage, errorMessage, isError, onMessageDismiss)

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    changePassword(oldPassword, newPassword)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = isNewPassValid && isConfirmPassValid && passwordsMatch
            ) {
                Text(update_password)
            }
        }
    }
}

@Composable
fun PasswordTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
) {
    var passwordVisible by remember { mutableStateOf(false) }

    TextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier,
        visualTransformation =
            if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            val icon =
                if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility
            val desc = if (passwordVisible) "Hide password" else "Show password"

            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(imageVector = icon, contentDescription = desc)
            }
        }
    )
}

@Composable
fun ChangePasswordToast(
    showCustomMessage: Boolean,
    errorMessage: String,
    isError: Boolean,
    onMessageDismiss: () -> Unit,
) {
    var showErrorMessage by remember(showCustomMessage) { mutableStateOf(showCustomMessage) }

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
}
