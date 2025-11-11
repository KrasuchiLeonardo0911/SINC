package com.sinc.mobile.app.features.movimiento.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.sinc.mobile.app.features.movimiento.MovimientoFormManager
import com.sinc.mobile.app.ui.components.FormDropdown
import com.sinc.mobile.ui.theme.colorAlta
import com.sinc.mobile.ui.theme.colorBaja
import com.sinc.mobile.ui.theme.colorBorde
import com.sinc.mobile.ui.theme.colorSuperficie

@Composable
fun MovimientoForm(
    formManager: MovimientoFormManager,
    selectedAction: String?,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
    isSaving: Boolean,
    saveError: String?,
    modifier: Modifier = Modifier
) {
    val formState = formManager.formState.value
    // Default to alta color if action is somehow null, though it shouldn't be
    val headerColor = if (selectedAction == "baja") colorBaja else colorAlta

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colorSuperficie),
        elevation = CardDefaults.cardElevation(4.dp),
        border = BorderStroke(1.dp, colorBorde)
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(headerColor)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Nueva ${selectedAction?.replaceFirstChar { it.uppercase() } ?: ""}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    color = Color.White
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar formulario", tint = Color.White)
                }
            }
            // Form Body
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FormDropdown(
                    items = formState.filteredEspecies,
                    label = "Especie",
                    selectedItem = formState.selectedEspecie,
                    onItemSelected = formManager::onEspecieSelected,
                    itemToString = { it.nombre },
                    modifier = Modifier.fillMaxWidth()
                )
                FormDropdown(
                    items = formState.filteredCategorias,
                    label = "Categoría",
                    selectedItem = formState.selectedCategoria,
                    onItemSelected = formManager::onCategoriaSelected,
                    itemToString = { it.nombre },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = formState.selectedEspecie != null
                )
                FormDropdown(
                    items = formState.filteredRazas,
                    label = "Raza",
                    selectedItem = formState.selectedRaza,
                    onItemSelected = formManager::onRazaSelected,
                    itemToString = { it.nombre },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = formState.selectedEspecie != null
                )
                FormDropdown(
                    items = formState.filteredMotivos,
                    label = "Motivo",
                    selectedItem = formState.selectedMotivo,
                    onItemSelected = formManager::onMotivoSelected,
                    itemToString = { it.nombre },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = formState.selectedEspecie != null
                )
                OutlinedTextField(
                    value = formState.cantidad,
                    onValueChange = formManager::onCantidadChanged,
                    label = { Text("Cantidad") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = formState.selectedEspecie != null
                )

                val isDestinoVisible = formState.selectedMotivo?.nombre?.contains("Traslado", ignoreCase = true) == true ||
                        formState.selectedMotivo?.nombre?.contains("Venta", ignoreCase = true) == true ||
                        formState.selectedMotivo?.nombre?.contains("Compra", ignoreCase = true) == true

                if (isDestinoVisible) {
                    OutlinedTextField(
                        value = formState.destino,
                        onValueChange = formManager::onDestinoChanged,
                        label = { Text("Destino/Origen") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(Modifier.height(4.dp))

                Button(
                    onClick = onSave,
                    enabled = formState.isFormValid && !isSaving,
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = headerColor)
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.Check, contentDescription = "Guardar")
                        Spacer(Modifier.width(8.dp))
                        Text("Guardar Movimiento", fontWeight = FontWeight.Bold)
                    }
                }

                saveError?.let {
                    Text(
                        text = "Error al guardar: $it",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}
