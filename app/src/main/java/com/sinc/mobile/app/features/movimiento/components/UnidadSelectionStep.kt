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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sinc.mobile.domain.model.UnidadProductiva
import com.sinc.mobile.ui.theme.colorAlta
import com.sinc.mobile.ui.theme.colorBorde
import com.sinc.mobile.ui.theme.colorSuperficie
import com.sinc.mobile.ui.theme.colorTextoPrincipal
import com.sinc.mobile.ui.theme.colorTextoSecundario

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
        elevation = CardDefaults.cardElevation(0.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colorSuperficie),
        border = BorderStroke(1.dp, colorBorde)
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
                        .background(colorAlta.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Place,
                        contentDescription = "Campo",
                        tint = colorAlta,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        selectedUnidad?.nombre ?: "Seleccionar campo",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = colorTextoPrincipal
                    )
                    // En la app real no tenemos localidad en el modelo, así que lo omitimos
                }
                Icon(
                    if (isDropdownExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = "Desplegar",
                    tint = colorTextoSecundario
                )
            }
            ExposedDropdownMenu(
                expanded = isDropdownExpanded,
                onDismissRequest = { onExpandedChange(false) },
                modifier = Modifier.background(colorSuperficie)
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
