package com.sinc.mobile.app.features.movimiento.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.sinc.mobile.app.ui.components.CustomDropdown
import com.sinc.mobile.domain.model.UnidadProductiva
import com.sinc.mobile.ui.theme.*

@Composable
fun UnidadSelectionStep(
    unidades: List<UnidadProductiva>,
    selectedUnidad: UnidadProductiva?,
    onUnidadSelected: (UnidadProductiva) -> Unit
) {
    
    CustomDropdown(
        items = unidades,
        selectedItem = selectedUnidad,
        onItemSelected = onUnidadSelected,
        getItemName = { it.nombre },
        placeholder = "Seleccionar campo",
        leadingIcon = { selected ->
            Icon(
                imageVector = Icons.Rounded.LocationOn,
                contentDescription = "Campo",
                tint = if (selected != null) SelectedPinYellow else InactiveGray,
                modifier = Modifier.size(28.dp) // Icono más grande
            )
        },
        itemLeadingIcon = { unidad, isSelected ->
            Icon(
                imageVector = Icons.Rounded.LocationOn,
                contentDescription = null,
                tint = if (isSelected) SelectedPinYellow else InactiveGray,
                modifier = Modifier.size(28.dp) // Icono más grande
            )
        }
    )
}
