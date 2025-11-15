package org.example.project.settings.deleteAccount

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import org.example.project.colors.AppColors.Red
import org.example.project.colors.AppColors.White
import org.example.project.components.alertDialog.AlertDialog
import org.example.project.components.customToast.CustomToastComposable
import org.example.project.components.loading.LoadingScreen
import org.example.project.dimens.Dimens.spacing24
import org.example.project.dimens.Dimens.spacing8
import org.example.project.strings.SmartTutorStrings.confirm_deletion
import org.example.project.strings.SmartTutorStrings.delete_account
import org.example.project.strings.SmartTutorStrings.delete_account_info
import org.example.project.strings.SmartTutorStrings.info_deletion
import org.example.project.strings.SmartTutorStrings.no
import org.example.project.strings.SmartTutorStrings.settings_title
import org.example.project.strings.SmartTutorStrings.yes
import org.example.project.utils.UtilsComposable.noRippleClickable

@Composable
fun DeleteAccountScreen(
    isLoading: Boolean,
    showCustomMessage: Boolean,
    message: String,
    isError: Boolean,
    onBack: () -> Unit,
    onDeleteConfirmed: () -> Unit,
    onCancel: () -> Unit,
    onMessageDismiss: () -> Unit,
) {
    var showConfirmDialog by remember { mutableStateOf(false) }
    var errorMessage by remember(message) { mutableStateOf(message) }
    ShowDeleteErrorMessage(
        showCustomMessage,
        errorMessage,
        isError,
        onMessageDismiss
    )

    if (isLoading) {
        LoadingScreen()
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(spacing24)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .noRippleClickable { onBack() }
                .padding(bottom = spacing24)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            Spacer(modifier = Modifier.width(spacing8))
            Text(
                text = settings_title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Text(
            text = delete_account,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(spacing24))

        Text(
            text = delete_account_info,
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { showConfirmDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = Red),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(delete_account, color = White)
        }

        if (showConfirmDialog) {
            AlertDialog(
                infoText = info_deletion,
                title = confirm_deletion,
                confirmationText = yes,
                cancelText = no,
                showConfirmDialog = { flag -> showConfirmDialog = flag },
                confirmButton = { onDeleteConfirmed() },
                onCancel = { onCancel() }
            )
        }
    }
}

@Composable
fun ShowDeleteErrorMessage(
    showCustomMessage: Boolean,
    errorMessage: String,
    isError: Boolean,
    onMessageDismiss: () -> Unit,
) {
    var showErrorMessage by remember(showCustomMessage) { mutableStateOf(showCustomMessage) }
    if (showErrorMessage && errorMessage.isNotEmpty()) {
        Box(modifier = Modifier.padding(spacing24)) {
            CustomToastComposable(
                isError = isError,
                message = errorMessage,
                initializeMessage = { showErrorMessage = true }
            )
        }

        LaunchedEffect(errorMessage) {
            delay(3000L)
            onMessageDismiss()
        }
    }
}