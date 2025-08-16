package org.example.project.screens.login

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
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
import org.example.project.components.button.AuthButton
import org.example.project.components.email.EmailTextField
import org.example.project.components.password.PasswordTextField
import org.example.project.dimens.Dimens.spacing12
import org.example.project.dimens.Dimens.spacing16
import org.example.project.dimens.Dimens.spacing24
import org.example.project.strings.SmartTutorStrings

@Composable
fun LoginComposable() {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxWidth().padding(spacing16),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        EmailTextField(value = email, onValueChange = { email = it })

        Spacer(Modifier.height(spacing12))

        PasswordTextField(value = password, onValueChange = { password = it })

        Spacer(Modifier.height(spacing24))

        AuthButton(
            text = SmartTutorStrings.login,
            onClick = {
                //toDo
            }
        )
    }
}