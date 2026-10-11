package org.example.project.registration.login

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
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
import org.example.project.components.password.PasswordTextFieldComposable
import org.example.project.dimens.Dimens.spacing12
import org.example.project.dimens.Dimens.spacing16
import org.example.project.dimens.Dimens.spacing24
import org.example.project.dimens.Dimens.spacing8
import org.example.project.dimens.Dimens.webSpacing
import org.example.project.strings.AppLanguage
import org.example.project.strings.SmartTutorStrings
import org.example.project.strings.SmartTutorStrings.passwordHint
import org.example.project.theme.SmartTutorStyles
import org.example.project.utils.validation.Validation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.ImeAction
@Composable
fun LoginComposable(
    login: (String, String) -> Unit,
    onForgotPasswordClick: () -> Unit,
    isWebUi: Boolean = false,
    currentLanguage: AppLanguage,
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val emailHasOnlyLatinCharacters =
        email.isNotBlank() && Validation.containsOnlyLatinEmailCharacters(email)

    val isEmailValid = Validation.isValidEmail(email)
    val isPasswordValid = Validation.isValidPassword(password)

    val isFormValid = emailHasOnlyLatinCharacters && isEmailValid && isPasswordValid
    val containerModifier = if (isWebUi) Modifier.width(webSpacing) else Modifier.fillMaxWidth()

    Column(
        modifier = containerModifier
            .verticalScroll(rememberScrollState())
            .padding(spacing16),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        EmailTextFieldComposable(
            value = email,
            onValueChange = { email = it },
            isWebUi = isWebUi,
            imeAction = ImeAction.Next,
        )
        if (email.isNotEmpty()) {
            Column(
                modifier = if (isWebUi) {
                    Modifier.width(webSpacing)
                } else {
                    Modifier.fillMaxWidth()
                },
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ValidationRule(
                    text = SmartTutorStrings.email_latin_only,
                    isValid = emailHasOnlyLatinCharacters
                )

                ValidationRule(
                    text = SmartTutorStrings.email_valid_format,
                    isValid = isEmailValid
                )
            }
        }

        Spacer(Modifier.height(spacing12))

        PasswordTextFieldComposable(
            value = password,
            onValueChange = { password = it },
            label = passwordHint,
            isWebUi = isWebUi,

            imeAction = ImeAction.Done,
            onDone = {
                if (isFormValid) {
                    login(email, password)
                }
            },

            )
        if (password.isNotEmpty()) {
            Column(
                modifier = if (isWebUi) {
                    Modifier.width(webSpacing)
                } else {
                    Modifier.fillMaxWidth()
                },
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ValidationRule(
                    text = SmartTutorStrings.password_min_length,
                    isValid = password.length >= 6
                )

                ValidationRule(
                    text = SmartTutorStrings.password_has_letter,
                    isValid = password.any { it.isLetter() }
                )

                ValidationRule(
                    text = SmartTutorStrings.password_has_number,
                    isValid = password.any { it.isDigit() }
                )

                ValidationRule(
                    text = SmartTutorStrings.password_has_special,
                    isValid = password.any { !it.isLetterOrDigit() }
                )
            }
        }
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

@Composable
private fun ValidationRule(
    text: String,
    isValid: Boolean,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = if (isValid) {
                Icons.Filled.CheckCircle
            } else {
                Icons.Filled.Cancel
            },
            contentDescription = null,
            tint = if (isValid) {
                Color(0xFF2E7D32)
            } else {
                MaterialTheme.colorScheme.error
            },
            modifier = Modifier.size(16.dp)
        )

        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = if (isValid) {
                Color(0xFF2E7D32)
            } else {
                MaterialTheme.colorScheme.error
            }
        )
    }
}
