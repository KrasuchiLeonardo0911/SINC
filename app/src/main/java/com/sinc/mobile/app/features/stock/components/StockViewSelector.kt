package com.sinc.mobile.app.features.stock.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sinc.mobile.app.ui.components.ExpandingDropdown
import com.sinc.mobile.app.ui.components.SoftDropdownIcon
import com.sinc.mobile.domain.model.UnidadProductiva
import com.sinc.mobile.ui.theme.CozyTextMain
import com.sinc.mobile.ui.theme.CozyWhite
import com.sinc.mobile.ui.theme.CozyYellow
import com.sinc.mobile.ui.theme.Gray200

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
                is UnidadProductiva -> it.nombre
                else -> "Seleccionar vista"
            }
        },
        placeholder = "Seleccionar vista",
        triggerIcon = {
            val isTotal = selectedView is String
            val iconColor = if (selectedView !is String || selectedView != "Total") CozyTextMain else CozyWhite
            SoftDropdownIcon(backgroundColor = CozyYellow) {
                Icon(
                    imageVector = if (isTotal) Icons.Rounded.GridView else Icons.Rounded.LocationOn,
                    contentDescription = "Vista",
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        },
        itemIcon = { item, isSelected ->
            val iconColor = if (isSelected) CozyTextMain else CozyWhite
            val icon = if (item is String) Icons.Rounded.GridView else Icons.Rounded.LocationOn
            SoftDropdownIcon(
                modifier = Modifier.size(32.dp),
                backgroundColor = CozyYellow
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(16.dp)
                )
            }
        },
        selectedItemBackgroundColor = Gray200,
        selectedItemTextColor = CozyTextMain,
        selectedCheckmarkColor = CozyWhite
    )
}
