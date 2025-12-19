package com.sinc.mobile.app.features.movimiento.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sinc.mobile.app.ui.components.SoftDropdown
import com.sinc.mobile.app.ui.components.SoftDropdownIcon
import com.sinc.mobile.domain.model.UnidadProductiva
import com.sinc.mobile.ui.theme.CozyWhite
import com.sinc.mobile.ui.theme.CozyYellow
import com.sinc.mobile.ui.theme.Gray200
import com.sinc.mobile.ui.theme.CozyTextMain // New import

@Composable
fun UnidadSelectionStep(
    unidades: List<UnidadProductiva>,
    selectedUnidad: UnidadProductiva?,
    onUnidadSelected: (UnidadProductiva) -> Unit
) {
    SoftDropdown(
        items = unidades,
        selectedItem = selectedUnidad,
        onItemSelected = onUnidadSelected,
        getItemName = { it.nombre },
        placeholder = "Seleccionar campo",
        triggerIcon = { _ -> // The 'selected' parameter is no longer used for color logic here
            val iconColor = if (selectedUnidad != null) CozyTextMain else CozyWhite // Changed DarkGreen to CozyTextMain
            SoftDropdownIcon(backgroundColor = CozyYellow) {
                Icon(
                    imageVector = Icons.Rounded.LocationOn,
                    contentDescription = "Campo",
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        },
        itemIcon = { item, isSelected -> // Keep item and isSelected if needed for other logic
            val iconColor = if (isSelected) CozyTextMain else CozyWhite // Changed DarkGreen to CozyTextMain
            SoftDropdownIcon(
                modifier = Modifier.size(32.dp),
                backgroundColor = CozyYellow
            ) {
                Icon(
                    imageVector = Icons.Rounded.LocationOn,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(16.dp)
                )
            }
        },
        selectedItemBackgroundColor = Gray200, // Changed CozyYellow to Gray200
        selectedItemTextColor = CozyTextMain, // Changed DarkGreen to CozyTextMain
        selectedCheckmarkColor = CozyWhite
    )
}
