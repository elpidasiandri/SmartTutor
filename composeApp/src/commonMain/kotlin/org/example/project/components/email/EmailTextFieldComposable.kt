package org.example.project.components.email

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import org.example.project.dimens.Dimens
import org.example.project.dimens.Dimens.webSpacing
import org.example.project.strings.SmartTutorStrings
import org.example.project.theme.SmartTutorStyles
import org.example.project.theme.SmartTutorStyles.defaultTextFieldColors

@Composable
fun EmailTextFieldComposable(
    value: String,
    onValueChange: (String) -> Unit,
    isWebUi: Boolean,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(SmartTutorStrings.emailHint) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        colors = defaultTextFieldColors(),
        modifier = if (isWebUi) {
            Modifier.padding(vertical = Dimens.spacing4).width(webSpacing)
        } else {
            SmartTutorStyles.textFieldModifier
        }
    )
}