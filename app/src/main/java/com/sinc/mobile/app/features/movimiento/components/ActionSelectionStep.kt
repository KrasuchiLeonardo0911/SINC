package com.sinc.mobile.app.features.movimiento.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ActionSelectionStep(
    onActionSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    selectedAction: String?
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ActionButton(
            text = "Registrar Alta",
            icon = Icons.Default.Add,
            isSelected = selectedAction == "alta",
            color = MaterialTheme.colorScheme.primary,
            onClick = { onActionSelected("alta") },
            modifier = Modifier.weight(1f)
        )
        ActionButton(
            text = "Registrar Baja",
            icon = Icons.Default.Remove,
            isSelected = selectedAction == "baja",
            color = MaterialTheme.colorScheme.error,
            onClick = { onActionSelected("baja") },
            modifier = Modifier.weight(1f)
        )
    }
}