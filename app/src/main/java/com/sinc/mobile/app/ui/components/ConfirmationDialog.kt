package com.sinc.mobile.app.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun ConfirmationDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    title: String,
    message: String,
    confirmButtonText: String = "Confirmar",
    dismissButtonText: String = "Cancelar",
    modifier: Modifier = Modifier
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(title) },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = onConfirm) {
                    Text(confirmButtonText)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(dismissButtonText)
                }
            },
            modifier = modifier
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewConfirmationDialog() {
    ConfirmationDialog(
        showDialog = true,
        onDismiss = { /*TODO*/ },
        onConfirm = { /*TODO*/ },
        title = "Confirmar Acción",
        message = "¿Estás seguro de que quieres realizar esta acción?"
    )
}
