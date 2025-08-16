package org.example.project.components.button

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import org.example.project.theme.SmartTutorStyles

@Composable
fun AuthButtonComposable(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean,
) {
    Button(
        onClick = onClick,
        modifier = SmartTutorStyles.buttonModifier,
        shape = SmartTutorStyles.buttonShape,
        enabled = enabled
    ) {
        Text(text = text)
    }
}
