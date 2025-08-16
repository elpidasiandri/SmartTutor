package org.example.project.theme

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

object SmartTutorStyles {
    val textFieldModifier = Modifier.Companion
        .fillMaxWidth()
        .padding(vertical = 4.dp)

    val buttonModifier = Modifier.Companion
        .fillMaxWidth()
        .padding(vertical = 8.dp)

    val buttonShape = RoundedCornerShape(12.dp)
}