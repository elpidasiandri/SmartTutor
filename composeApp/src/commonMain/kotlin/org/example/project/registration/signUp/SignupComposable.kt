package org.example.project.registration.signUp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import org.example.project.components.button.ButtonComposable
import org.example.project.components.email.EmailTextFieldComposable
import org.example.project.components.errorText.ErrorTextComposable
import org.example.project.components.password.PasswordTextFieldComposable
import org.example.project.dimens.Dimens.spacing16
import org.example.project.dimens.Dimens.spacing8
import org.example.project.dimens.Dimens.webSpacing
import org.example.project.strings.SmartTutorStrings
import org.example.project.strings.SmartTutorStrings.email_dismatch
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
        email == email2 && email.isNotEmpty() && email2.isNotEmpty()
    val isFormValid by derivedStateOf {
        isEmailValid &&
                isPassword1Valid && isPassword2Valid &&
                arePasswordsSame && isUsernameValid &&
                areEmailsSame && isUsernameValid
    }

    Column(
        modifier = Modifier.padding(spacing16),
        verticalArrangement = Arrangement.spacedBy(spacing8)
    ) {
        EmailTextFieldComposable(value = email, onValueChange = { email = it }, isWebUi = isWebUi)

        if (!isEmailValid && email.isNotEmpty()) {
            ErrorTextComposable(SmartTutorStrings.invalid_email)
        }
        EmailTextFieldComposable(
            value = email2,
            onValueChange = { email2 = it },
            isWebUi = isWebUi
        )

        if (!isEmail2Valid && email2.isNotEmpty()) {
            ErrorTextComposable(SmartTutorStrings.invalid_email)
        }

        if (
            email.isNotEmpty() &&
            email2.isNotEmpty() &&
            isEmailValid &&
            isEmail2Valid &&
            !areEmailsSame
        ) {
            ErrorTextComposable(email_dismatch)
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

        if (password1.length > 5 && !isPassword1Valid) {
            ErrorTextComposable(SmartTutorStrings.invalid_password)
        }

        PasswordTextFieldComposable(
            value = password2,
            onValueChange = { password2 = it },
            label = SmartTutorStrings.confirm_password,
            isWebUi = isWebUi
        )

        if (password2.length > 5 && !isPassword2Valid) {
            ErrorTextComposable(SmartTutorStrings.invalid_password)
        }
        if (password1.isNotEmpty() &&
            password2.isNotEmpty() &&
            isPassword1Valid &&
            isPassword2Valid &&
            !arePasswordsSame
        ) {
            ErrorTextComposable(SmartTutorStrings.password_dismatch)
        }
        ButtonComposable(
            text = SmartTutorStrings.signup,
            enabled = isFormValid,
            onClick = {
                signUp(email, password1, username)
            },
            modifier = if (isWebUi) Modifier.width(webSpacing)
                .padding(vertical = spacing8)
                .align(Alignment.CenterHorizontally) else SmartTutorStyles.buttonModifier
        )
    }
}
