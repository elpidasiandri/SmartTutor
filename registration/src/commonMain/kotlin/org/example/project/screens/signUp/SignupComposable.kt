package org.example.project.screens.signUp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import org.example.project.components.button.AuthButton
import org.example.project.components.email.EmailTextField
import org.example.project.components.password.PasswordTextField
import org.example.project.dimens.Dimens.spacing16
import org.example.project.dimens.Dimens.spacing8
import org.example.project.strings.SmartTutorStrings

@Composable
fun SignupComposable() {
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password1 by remember { mutableStateOf("") }
    var password2 by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.padding(spacing16),
        verticalArrangement = Arrangement.spacedBy(spacing8)
    ) {
        EmailTextField(value = email, onValueChange = { email = it })

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text(SmartTutorStrings.username) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        PasswordTextField(
            value = password1,
            onValueChange = { password1 = it },
            label = SmartTutorStrings.passwordHint
        )

        PasswordTextField(
            value = password2,
            onValueChange = { password2 = it },
            label = SmartTutorStrings.confirm_password
        )

        AuthButton(
            text = SmartTutorStrings.signup,
            onClick = {
                // TODO
            }
        )
    }
}
