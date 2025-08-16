package org.example.project.theme

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.example.project.colors.AppColors
import org.example.project.dimens.Dimens

object SmartTutorStyles {
    val textFieldModifier = Modifier.Companion
        .fillMaxWidth()
        .padding(vertical = Dimens.spacing4)

    val buttonModifier = Modifier.Companion
        .fillMaxWidth()
        .padding(vertical = Dimens.spacing8)

    val buttonShape = RoundedCornerShape(Dimens.spacing12)

    @Composable
    fun defaultTextFieldColors(): TextFieldColors =
        OutlinedTextFieldDefaults.colors(
            focusedTextColor = AppColors.Black,
            unfocusedTextColor = AppColors.GrayDark,
            cursorColor = AppColors.Black,
            errorTextColor = AppColors.Red,
            focusedBorderColor = AppColors.Purple ,
            unfocusedBorderColor = AppColors.GrayDark,
            errorBorderColor = AppColors.Red,
            selectionColors = TextSelectionColors(AppColors.Blue, AppColors.GrayLight)
        )
}