package org.example.project.registration.login

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.example.project.components.button.ButtonComposable
import org.example.project.components.email.EmailTextFieldComposable
import org.example.project.components.errorText.ErrorTextComposable
import org.example.project.components.password.PasswordTextFieldComposable
import org.example.project.dimens.Dimens.spacing12
import org.example.project.dimens.Dimens.spacing16
import org.example.project.dimens.Dimens.spacing24
import org.example.project.dimens.Dimens.spacing8
import org.example.project.dimens.Dimens.webSpacing
import org.example.project.strings.SmartTutorStrings
import org.example.project.strings.SmartTutorStrings.passwordHint
import org.example.project.theme.SmartTutorStyles
import org.example.project.utils.Validation

@Composable
fun LoginComposable(
    login: (String, String) -> Unit,
    onForgotPasswordClick: () -> Unit,
    isWebUi: Boolean = false,
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val isEmailValid = Validation.isValidEmail(email)
    val isPasswordValid = Validation.isValidPassword(password)
    val isFormValid = isEmailValid && isPasswordValid
    val containerModifier = if (isWebUi) Modifier.width(webSpacing) else Modifier.fillMaxWidth()

    Column(
        modifier = containerModifier
            .padding(spacing16),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        EmailTextFieldComposable(
            value = email,
            onValueChange = { email = it },
            isWebUi = isWebUi
        )
        if (!isEmailValid && email.isNotEmpty()) {
            ErrorTextComposable(SmartTutorStrings.invalid_email)
        }

        Spacer(Modifier.height(spacing12))

        PasswordTextFieldComposable(
            value = password,
            onValueChange = { password = it },
            label = passwordHint,
            isWebUi = isWebUi
        )

        Spacer(Modifier.height(spacing24))

        ButtonComposable(
            text = SmartTutorStrings.login,
            enabled = isFormValid,
            modifier = if (isWebUi) Modifier.width(webSpacing)
                .padding(vertical = spacing8)
                .align(Alignment.CenterHorizontally) else SmartTutorStyles.buttonModifier,
            onClick = {
                login(email, password)
            }
        )
        val modifierWidthButton = if (isWebUi) Modifier.width(webSpacing)
            .align(Alignment.CenterHorizontally) else Modifier.fillMaxWidth()
        TextButton(
            onClick = {
                onForgotPasswordClick()
            },
            modifier = modifierWidthButton.padding(top = spacing12)
        ) {
            Text(
                text = SmartTutorStrings.forgot_password,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}