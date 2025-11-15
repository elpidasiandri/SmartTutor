package org.example.project.components.alertDialog

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun AlertDialog(
    infoText:String,
    title:String,
    confirmationText:String,
    cancelText:String,
    showConfirmDialog:(Boolean)-> Unit,
    confirmButton :()-> Unit,
    onCancel :()-> Unit,
) {
    AlertDialog(
        onDismissRequest = { showConfirmDialog(false)},
        title = { Text(title) },
        text = { Text(infoText) },
        confirmButton = {
            TextButton(onClick = {
                showConfirmDialog(false)
                confirmButton()
            }) {
                Text(confirmationText)
            }
        },
        dismissButton = {
            TextButton(onClick = {
                showConfirmDialog(false)
                onCancel()
            }) {
                Text(cancelText)
            }
        }
    )
}