package org.example.project.components.button

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.example.project.theme.SmartTutorStyles

@Composable
fun ButtonComposable(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = SmartTutorStyles.buttonShape,
        enabled = enabled
    ) {
        Text(text = text)
    }
}
