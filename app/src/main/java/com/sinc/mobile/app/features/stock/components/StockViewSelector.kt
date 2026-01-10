package com.sinc.mobile.app.features.stock.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.GridView
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
fun StockViewSelector(
    unidades: List<UnidadProductiva>,
    selectedView: Any,
    onSelectionChanged: (Any) -> Unit
) {
    val items = listOf<Any>("Total") + unidades

    ExpandingDropdown(
        items = items,
        selectedItem = selectedView,
        onItemSelected = onSelectionChanged,
        getItemName = {
            when (it) {
                is String -> it
                is UnidadProductiva -> it.nombre ?: "Sin nombre"
                else -> "Seleccionar vista"
            }
        },
        placeholder = "Seleccionar vista",
        triggerIcon = {
            val isTotal = selectedView is String
            val iconColor = MaterialTheme.colorScheme.onPrimary
            SoftDropdownIcon(backgroundColor = MaterialTheme.colorScheme.primary) {
                Icon(
                    imageVector = if (isTotal) Icons.Rounded.GridView else Icons.Rounded.LocationOn,
                    contentDescription = "Vista",
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        },
        itemIcon = { item, isSelected ->
            val iconColor = MaterialTheme.colorScheme.onPrimary
            val icon = if (item is String) Icons.Rounded.GridView else Icons.Rounded.LocationOn
            SoftDropdownIcon(
                modifier = Modifier.size(32.dp),
                backgroundColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = icon,
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
