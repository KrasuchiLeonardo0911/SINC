package com.sinc.mobile.app.features.movimiento.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sinc.mobile.domain.model.UnidadProductiva

@Composable
fun CampoListItem(
    unidad: UnidadProductiva,
    isEnabled: Boolean,
    onUnidadSelected: (UnidadProductiva) -> Unit
) {
    val alpha = if (isEnabled) 1f else 0.5f // Reduce opacity for disabled items

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isEnabled) {
                if (isEnabled) {
                    onUnidadSelected(unidad)
                }
            }
            .alpha(alpha)
            .padding(vertical = 12.dp, horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = unidad.nombre,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = if (isEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                if (isEnabled) {
                    Text(
                        text = "RNSPA: ${unidad.identificadorLocal ?: "No disponible"}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                } else {
                    Text(
                        text = "No hay un área disponible para el registro.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.error // Or a distinct gray for errors
                        )
                    )
                }
            }
            if (isEnabled) {
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = "Seleccionar ${unidad.nombre}",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
    Divider(
        modifier = Modifier.padding(horizontal = 16.dp),
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
    )
}
