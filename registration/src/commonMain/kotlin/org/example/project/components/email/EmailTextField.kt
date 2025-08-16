package org.example.project.components.email

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.input.KeyboardType
import org.example.project.strings.SmartTutorStrings
import org.example.project.theme.SmartTutorStyles

@Composable
fun EmailTextField(
    value: String,
    onValueChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(SmartTutorStrings.emailHint) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        modifier = SmartTutorStyles.textFieldModifier
    )
}