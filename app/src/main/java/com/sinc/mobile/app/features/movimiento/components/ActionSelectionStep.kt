package com.sinc.mobile.app.features.movimiento.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sinc.mobile.ui.theme.colorAlta
import com.sinc.mobile.ui.theme.colorBaja

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
            color = colorAlta,
            onClick = { onActionSelected("alta") },
            modifier = Modifier.weight(1f)
        )
        ActionButton(
            text = "Registrar Baja",
            icon = Icons.Default.Remove,
            isSelected = selectedAction == "baja",
            color = colorBaja,
            onClick = { onActionSelected("baja") },
            modifier = Modifier.weight(1f)
        )
    }
}
