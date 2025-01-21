package com.alekey.bingo.ui.dialogs

import androidx.compose.runtime.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text

@Composable
fun ConfirmationDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    title: String,
    message: String
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(title) },
            text = { Text(message) },
            confirmButton = {
                Button(onClick = onConfirm) {
                    Text("Sí")
                }
            },
            dismissButton = {
                Button(onClick = onDismiss) {
                    Text("No")
                }
            }
        )
    }
}