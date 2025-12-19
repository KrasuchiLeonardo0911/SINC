package com.sinc.mobile.app.features.movimiento.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sinc.mobile.app.features.movimiento.MovimientoFormState
import com.sinc.mobile.app.ui.components.QuantitySelector
import com.sinc.mobile.app.ui.components.SoftDropdown
import com.sinc.mobile.app.ui.components.SoftDropdownIcon
import com.sinc.mobile.domain.model.Categoria
import com.sinc.mobile.domain.model.Especie
import com.sinc.mobile.domain.model.MotivoMovimiento
import com.sinc.mobile.domain.model.Raza
import com.sinc.mobile.ui.theme.*

@Composable
fun MovimientoForm(
    modifier: Modifier = Modifier,
    formState: MovimientoFormState,
    onEspecieSelected: (Especie) -> Unit,
    onCategoriaSelected: (Categoria) -> Unit,
    onRazaSelected: (Raza) -> Unit,
    onMotivoSelected: (MotivoMovimiento) -> Unit,
    onCantidadChanged: (String) -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CozyWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // --- Especie ---
            Column {
                Text(
                    text = "Especie",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = CozyTextMain,
                )
                Spacer(Modifier.height(8.dp))
                SoftDropdown(
                    items = formState.filteredEspecies,
                    selectedItem = formState.selectedEspecie,
                    onItemSelected = onEspecieSelected,
                    getItemName = { it.nombre },
                    placeholder = "Seleccionar especie",
                    triggerIcon = { selected ->
                        SoftDropdownIcon {
                            if (selected != null) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Seleccionado",
                                    tint = CozyTextMain,
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.TouchApp,
                                    contentDescription = "Seleccionar",
                                    tint = CozyWhite,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    },
                    itemIcon = { _, _ -> },
                    showItemIcons = false,
                    selectedItemBackgroundColor = Gray200,
                    selectedItemTextColor = CozyTextMain,
                    selectedCheckmarkColor = CozyTextMain,
                )
            }

            // --- Categoria ---
            Column {
                Text(
                    text = "Categoría",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = CozyTextMain,
                )
                Spacer(Modifier.height(8.dp))
                SoftDropdown(
                    items = formState.filteredCategorias,
                    selectedItem = formState.selectedCategoria,
                    onItemSelected = onCategoriaSelected,
                    getItemName = { it.nombre },
                    placeholder = "Seleccionar categoría",
                    enabled = formState.selectedEspecie != null,
                    triggerIcon = { selected ->
                        SoftDropdownIcon {
                            if (selected != null) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Seleccionado",
                                    tint = CozyTextMain,
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.TouchApp,
                                    contentDescription = "Seleccionar",
                                    tint = CozyWhite,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    },
                    itemIcon = { _, _ -> },
                    showItemIcons = false,
                    selectedItemBackgroundColor = Gray200,
                    selectedItemTextColor = CozyTextMain,
                    selectedCheckmarkColor = CozyTextMain,
                )
            }

            // --- Raza ---
            Column {
                Text(
                    text = "Raza",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = CozyTextMain,
                )
                Spacer(Modifier.height(8.dp))
                SoftDropdown(
                    items = formState.filteredRazas,
                    selectedItem = formState.selectedRaza,
                    onItemSelected = onRazaSelected,
                    getItemName = { it.nombre },
                    placeholder = "Seleccionar raza",
                    enabled = formState.selectedEspecie != null,
                    triggerIcon = { selected ->
                        SoftDropdownIcon {
                            if (selected != null) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Seleccionado",
                                    tint = CozyTextMain,
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.TouchApp,
                                    contentDescription = "Seleccionar",
                                    tint = CozyWhite,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    },
                    itemIcon = { _, _ -> },
                    showItemIcons = false,
                    selectedItemBackgroundColor = Gray200,
                    selectedItemTextColor = CozyTextMain,
                    selectedCheckmarkColor = CozyTextMain,
                )
            }

            // --- Motivo ---
            Column {
                Text(
                    text = "Motivo",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = CozyTextMain,
                )
                Spacer(Modifier.height(8.dp))
                SoftDropdown(
                    items = formState.filteredMotivos,
                    selectedItem = formState.selectedMotivo,
                    onItemSelected = onMotivoSelected,
                    getItemName = { it.nombre },
                    placeholder = "Seleccionar motivo",
                    triggerIcon = { selected ->
                        SoftDropdownIcon {
                            if (selected != null) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Seleccionado",
                                    tint = CozyTextMain,
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.TouchApp,
                                    contentDescription = "Seleccionar",
                                    tint = CozyWhite,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    },
                    itemIcon = { _, _ -> },
                    showItemIcons = false,
                    selectedItemBackgroundColor = Gray200,
                    selectedItemTextColor = CozyTextMain,
                    selectedCheckmarkColor = CozyTextMain,
                )
            }

            // --- Cantidad ---
            QuantitySelector(
                label = "Cantidad",
                quantity = formState.cantidad,
                onQuantityChange = onCantidadChanged
            )
        }
    }
}