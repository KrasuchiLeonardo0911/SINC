package com.sinc.mobile.app.features.movimiento.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sinc.mobile.app.ui.components.ExpandingDropdown
import com.sinc.mobile.app.ui.components.SoftDropdownIcon
import com.sinc.mobile.domain.model.UnidadProductiva

@Composable
fun UnidadSelectionStep(
    unidades: List<UnidadProductiva>,
    selectedUnidad: UnidadProductiva?,
    onUnidadSelected: (UnidadProductiva) -> Unit
) {
    ExpandingDropdown(
        items = unidades,
        selectedItem = selectedUnidad,
        onItemSelected = onUnidadSelected,
        getItemName = { it.nombre ?: "Sin nombre" },
        placeholder = "Seleccionar campo",
        triggerIcon = { _ ->
            val iconColor = MaterialTheme.colorScheme.onPrimary
            SoftDropdownIcon(backgroundColor = MaterialTheme.colorScheme.primary) {
                Icon(
                    imageVector = Icons.Rounded.LocationOn,
                    contentDescription = "Campo",
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        },
        itemIcon = { item, isSelected ->
            val iconColor = MaterialTheme.colorScheme.onPrimary
            SoftDropdownIcon(
                modifier = Modifier.size(32.dp),
                backgroundColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Rounded.LocationOn,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(16.dp)
                )
            }
        },
        selectedItemBackgroundColor = MaterialTheme.colorScheme.surfaceVariant,
        selectedItemTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
        selectedCheckmarkColor = MaterialTheme.colorScheme.onSurfaceVariant
    )
}