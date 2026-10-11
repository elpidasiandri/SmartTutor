package org.example.project.components.password

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import org.example.project.dimens.Dimens
import org.example.project.dimens.Dimens.webSpacing
import org.example.project.theme.SmartTutorStyles
import org.example.project.theme.SmartTutorStyles.defaultTextFieldColors
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
@Composable
fun PasswordTextFieldComposable(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isWebUi: Boolean,
    imeAction: ImeAction = ImeAction.Next,
    onDone: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var passwordVisible by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(
                    imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                    contentDescription = if (passwordVisible) "Hide password" else "Show password"
                )
            }
        },

        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = imeAction
        ),
        keyboardActions = KeyboardActions(
            onNext = {
                focusManager.moveFocus(FocusDirection.Next)
            },
            onDone = {
                onDone()
            }
        ),

        modifier = modifier.then(
            if (isWebUi) {
                Modifier.padding(vertical = Dimens.spacing4).width(webSpacing)
            } else {
                SmartTutorStyles.textFieldModifier
            }
        ),
        colors = defaultTextFieldColors()
    )
}