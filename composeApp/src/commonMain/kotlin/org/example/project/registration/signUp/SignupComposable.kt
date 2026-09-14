package org.example.project.registration.signUp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.example.project.components.button.ButtonComposable
import org.example.project.components.email.EmailTextFieldComposable
import org.example.project.components.errorText.ErrorTextComposable
import org.example.project.components.password.PasswordTextFieldComposable
import org.example.project.dimens.Dimens.spacing16
import org.example.project.dimens.Dimens.spacing8
import org.example.project.dimens.Dimens.webSpacing
import org.example.project.strings.SmartTutorStrings
import org.example.project.theme.SmartTutorStyles
import org.example.project.theme.SmartTutorStyles.defaultTextFieldColors
import org.example.project.utils.validation.Validation


@Composable
fun SignupComposable(
    signUp: (String, String, String) -> Unit,
    isWebUi: Boolean = false,
) {
    var email by remember { mutableStateOf("") }
    var email2 by remember { mutableStateOf("") }
    val emailHasOnlyLatinCharacters =
        email.isNotBlank() && Validation.containsOnlyLatinEmailCharacters(email)

    val email2HasOnlyLatinCharacters =
        email2.isNotBlank() && Validation.containsOnlyLatinEmailCharacters(email2)


    var username by remember { mutableStateOf("") }
    var password1 by remember { mutableStateOf("") }
    var password2 by remember { mutableStateOf("") }

    val isEmailValid = Validation.isValidEmail(email)
    val isEmail2Valid = Validation.isValidEmail(email2)

    val isUsernameValid = username.isNotBlank()
    val isPassword1Valid = Validation.isValidPassword(password1)
    val isPassword2Valid = Validation.isValidPassword(password2)
    val arePasswordsSame: Boolean =
        password1 == password2 && password1.isNotEmpty() && password2.isNotEmpty()
    val areEmailsSame =
        email.trim() == email2.trim() && email.trim().isNotEmpty() && email2.trim().isNotEmpty()
    val isFormValid by derivedStateOf {
        isEmailValid &&
                isPassword1Valid && isPassword2Valid &&
                arePasswordsSame && isUsernameValid &&
                areEmailsSame && isUsernameValid
    }
    var showVerificationInfo by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()).padding(spacing16),
        verticalArrangement = Arrangement.spacedBy(spacing8),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        EmailTextFieldComposable(value = email, onValueChange = { email = it }, isWebUi = isWebUi)

        if (email.isNotEmpty()) {
            Column(
                modifier = if (isWebUi) Modifier.width(webSpacing) else Modifier.fillMaxWidth(),
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
        EmailTextFieldComposable(
            value = email2,
            onValueChange = { email2 = it },
            labelHint = SmartTutorStrings.confirm_email,
            isWebUi = isWebUi
        )
        if (email2.isNotEmpty()) {
            Column(
                modifier = if (isWebUi) Modifier.width(webSpacing) else Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ValidationRule(
                    text = SmartTutorStrings.email_latin_only,
                    isValid = email2HasOnlyLatinCharacters
                )

                ValidationRule(
                    text = SmartTutorStrings.email_valid_format,
                    isValid = isEmail2Valid
                )

                ValidationRule(
                    text = SmartTutorStrings.emails_match,
                    isValid = areEmailsSame
                )
            }
        }

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text(SmartTutorStrings.username) },
            singleLine = true,
            modifier = if (isWebUi) Modifier.width(webSpacing) else Modifier.fillMaxWidth(),
            colors = defaultTextFieldColors()
        )

        if (!isUsernameValid && username.isNotEmpty()) {
            ErrorTextComposable(SmartTutorStrings.username_not_empty)
        }

        PasswordTextFieldComposable(
            value = password1,
            onValueChange = { password1 = it },
            label = SmartTutorStrings.passwordHint,
            isWebUi = isWebUi
        )

        if (password1.isNotEmpty()) {
            Column(
                modifier = if (isWebUi) Modifier.width(webSpacing) else Modifier.fillMaxWidth()
            ) {
                ValidationRule(
                    text = SmartTutorStrings.password_min_length,
                    isValid = password1.length >= 6
                )

                ValidationRule(
                    text = SmartTutorStrings.password_has_letter,
                    isValid = password1.any { it.isLetter() }
                )

                ValidationRule(
                    text = SmartTutorStrings.password_has_number,
                    isValid = password1.any { it.isDigit() }
                )

                ValidationRule(
                    text = SmartTutorStrings.password_has_special,
                    isValid = password1.any { !it.isLetterOrDigit() }
                )
            }
        }

        PasswordTextFieldComposable(
            value = password2,
            onValueChange = { password2 = it },
            label = SmartTutorStrings.confirm_password,
            isWebUi = isWebUi
        )

        if (password2.isNotEmpty()) {
            Column(
                modifier = if (isWebUi) Modifier.width(webSpacing) else Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {

                ValidationRule(
                    text = SmartTutorStrings.passwords_match,
                    isValid = arePasswordsSame
                )

            }
        }
        ButtonComposable(
            text = SmartTutorStrings.signup,
            enabled = isFormValid,
            onClick = {
                signUp(email.trim(), password1, username)
                showVerificationInfo = true
            },
            modifier = if (isWebUi) Modifier.width(webSpacing)
                .padding(vertical = spacing8)
                .align(Alignment.CenterHorizontally) else SmartTutorStyles.buttonModifier
        )
        if (showVerificationInfo) {
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = SmartTutorStrings.verification_email_sent_info,
                style = MaterialTheme.typography.bodyMedium,
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