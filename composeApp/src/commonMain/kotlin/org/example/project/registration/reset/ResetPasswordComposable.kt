package org.example.project.registration.reset

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import org.example.project.colors.AppColors
import org.example.project.components.button.ButtonComposable
import org.example.project.components.email.EmailTextFieldComposable
import org.example.project.components.errorText.ErrorTextComposable
import org.example.project.dimens.Dimens.spacing16
import org.example.project.dimens.Dimens.spacing24
import org.example.project.strings.SmartTutorStrings
import org.example.project.utils.Validation

@Composable
fun ResetPasswordComposable(
    onBackToLogin: () -> Unit,
    resetPassword: (String) -> Unit,
) {
    var email by remember { mutableStateOf("") }
    val isEmailValid = Validation.isValidEmail(email)
    val isFormValid by derivedStateOf { isEmailValid }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(spacing16),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = SmartTutorStrings.reset_password,
            modifier = Modifier.padding(bottom = spacing16),
            style = TextStyle(
                color = AppColors.Purple,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        )

        EmailTextFieldComposable(value = email, onValueChange = { email = it })

        if (!isEmailValid && email.isNotEmpty()) {
            ErrorTextComposable(SmartTutorStrings.invalid_email)
        }

        Spacer(Modifier.height(spacing24))

        Text(
            text = SmartTutorStrings.reset_info,
            style = TextStyle(
                color = AppColors.GrayDark,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = spacing16)
        )

        Spacer(Modifier.height(spacing24))
        ButtonComposable(
            text = SmartTutorStrings.submit,
            enabled = isFormValid,
            onClick = {
                resetPassword(email)
            }
        )
        ButtonComposable(
            text = SmartTutorStrings.back_to_login,
            enabled = true,
            onClick = {
                onBackToLogin()
            }
        )
    }
}
