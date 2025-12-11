package com.sinc.mobile.app.features.movimiento.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow // Importación añadida
import androidx.compose.ui.graphics.Color // Importación añadida
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp // Importación añadida
import com.sinc.mobile.domain.model.UnidadProductiva
import com.sinc.mobile.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnidadSelectionStep(
    unidades: List<UnidadProductiva>,
    selectedUnidad: UnidadProductiva?,
    isDropdownExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onUnidadSelected: (UnidadProductiva) -> Unit
) {
    Card(
        modifier = Modifier.shadow(elevation = 4.dp, shape = RoundedCornerShape(24.dp), clip = false),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CozyWhite),
    ) {
        ExposedDropdownMenuBox(
            expanded = isDropdownExpanded,
            onExpandedChange = onExpandedChange
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(PastelGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Place,
                        contentDescription = "Campo",
                        tint = Color(0xFF388E3C), // Verde oscuro para alto contraste
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        selectedUnidad?.nombre ?: "Seleccionar campo",
                        fontWeight = FontWeight.Medium,
                        fontSize = 18.sp,
                        color = CozyTextMain
                    )
                }
                Icon(
                    if (isDropdownExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = "Desplegar",
                    tint = CozyIconGray
                )
            }
            ExposedDropdownMenu(
                expanded = isDropdownExpanded,
                onDismissRequest = { onExpandedChange(false) },
                modifier = Modifier.background(CozyWhite)
            ) {
                unidades.forEach { unidad ->
                    DropdownMenuItem(
                        text = { Text(unidad.nombre) },
                        onClick = {
                            onUnidadSelected(unidad)
                        }
                    )
                }
            }
        }
    }
}
