package org.example.project.registration.login

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import org.example.project.strings.SmartTutorStrings
import org.example.project.strings.SmartTutorStrings.passwordHint
import org.example.project.utils.Validation
@Composable
fun LoginComposable(login: (String, String) -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val isEmailValid = Validation.isValidEmail(email)
    val isPasswordValid = Validation.isValidPassword(password)
    val isFormValid = isEmailValid && isPasswordValid


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(spacing16),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        EmailTextFieldComposable(value = email, onValueChange = { email = it })

        Spacer(Modifier.height(spacing12))

        PasswordTextFieldComposable(
            value = password,
            onValueChange = { password = it },
            label = passwordHint
        )

        Spacer(Modifier.height(spacing24))

        ButtonComposable(
            text = SmartTutorStrings.login,
            enabled = isFormValid,
            onClick = {
                login(email, password)
            }
        )
    }
}