package org.example.project.components.errorText

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.example.project.colors.AppColors
import org.example.project.dimens.Dimens

@Composable
fun ErrorTextComposable(errorMessage: String) {
    Text(
        modifier = Modifier.padding(start = Dimens.spacing16, top = Dimens.spacing4),
        text = errorMessage,
        color = AppColors.Red
    )
}