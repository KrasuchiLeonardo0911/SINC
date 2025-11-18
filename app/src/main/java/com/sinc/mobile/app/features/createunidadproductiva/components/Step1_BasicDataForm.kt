package com.sinc.mobile.app.features.createunidadproductiva.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.sinc.mobile.app.features.createunidadproductiva.CreateUnidadProductivaState
import com.sinc.mobile.domain.model.CondicionTenencia
import com.sinc.mobile.domain.model.Municipio

@Composable
fun Step1_BasicDataForm(
    uiState: CreateUnidadProductivaState,
    onNombreChange: (String) -> Unit,
    onIdentificadorLocalChange: (String) -> Unit,
    onSuperficieChange: (String) -> Unit,
    onMunicipioSelected: (Municipio) -> Unit,
    onCondicionTenenciaSelected: (CondicionTenencia) -> Unit,
    onHabitaChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Card 1: Datos del Establecimiento
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Datos del Establecimiento", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = uiState.nombre,
                    onValueChange = onNombreChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Nombre del campo *") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = uiState.identificadorLocal,
                    onValueChange = onIdentificadorLocalChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("N° de Identificador (RNSPA) *") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = uiState.superficie,
                    onValueChange = onSuperficieChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Superficie (ha) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }
        }

        // Card 2: Ubicación y Tenencia
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Ubicación y Tenencia", style = MaterialTheme.typography.titleMedium)

                // Dropdown para Municipio
                var expandedMunicipio by remember { mutableStateOf(false) }
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = uiState.selectedMunicipio?.nombre ?: "Seleccionar municipio",
                        onValueChange = { /* Read-only */ },
                        label = { Text("Municipio *") },
                        readOnly = true,
                        trailingIcon = {
                            Icon(Icons.Default.ArrowDropDown, "contentDescription", Modifier.clickable { expandedMunicipio = !expandedMunicipio })
                        },
                        modifier = Modifier.fillMaxWidth().clickable { expandedMunicipio = !expandedMunicipio }
                    )
                    DropdownMenu(
                        expanded = expandedMunicipio,
                        onDismissRequest = { expandedMunicipio = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        uiState.catalogos?.municipios?.forEach { municipio ->
                            DropdownMenuItem(
                                text = { Text(municipio.nombre) },
                                onClick = {
                                    onMunicipioSelected(municipio)
                                    expandedMunicipio = false
                                }
                            )
                        }
                    }
                }

                // Campo de Paraje deshabilitado
                OutlinedTextField(
                    value = "Primero elija un municipio",
                    onValueChange = { /* Read-only */ },
                    label = { Text("Paraje / Colonia (Opcional)") },
                    readOnly = true,
                    enabled = false,
                    modifier = Modifier.fillMaxWidth()
                )

                // Dropdown para Condición de Tenencia
                var expandedCondicionTenencia by remember { mutableStateOf(false) }
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = uiState.selectedCondicionTenencia?.nombre ?: "Seleccionar condición",
                        onValueChange = { /* Read-only */ },
                        label = { Text("Condición de Tenencia *") },
                        readOnly = true,
                        trailingIcon = {
                            Icon(Icons.Default.ArrowDropDown, "contentDescription", Modifier.clickable { expandedCondicionTenencia = !expandedCondicionTenencia })
                        },
                        modifier = Modifier.fillMaxWidth().clickable { expandedCondicionTenencia = !expandedCondicionTenencia }
                    )
                    DropdownMenu(
                        expanded = expandedCondicionTenencia,
                        onDismissRequest = { expandedCondicionTenencia = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        uiState.catalogos?.condicionesTenencia?.forEach { condicion ->
                            DropdownMenuItem(
                                text = { Text(condicion.nombre) },
                                onClick = {
                                    onCondicionTenenciaSelected(condicion)
                                    expandedCondicionTenencia = false
                                }
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = uiState.habita,
                        onCheckedChange = onHabitaChange
                    )
                    Text(
                        text = "Habita en el lugar",
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}