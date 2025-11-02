package com.sinc.mobile.app.features.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.ui.text.input.KeyboardType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
    val state = viewModel.state.value

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (state.isLoading) {
            CircularProgressIndicator()
        }

        state.error?.let {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "Error: $it", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.error)
                Button(onClick = { viewModel.loadUnidadesProductivas() }, modifier = Modifier.padding(top = 8.dp)) {
                    Text("Reintentar")
                }
            }
        }

        if (state.unidades.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Seleccione un campo",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                ExposedDropdownMenuBox(
                    expanded = state.isDropdownExpanded,
                    onExpandedChange = { viewModel.onDropdownExpandedChange(it) }
                ) {
                    TextField(
                        value = state.selectedUnidad?.nombre ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Campo") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = state.isDropdownExpanded)
                        },
                        colors = ExposedDropdownMenuDefaults.textFieldColors(),
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = state.isDropdownExpanded,
                        onDismissRequest = { viewModel.onDropdownExpandedChange(false) }
                    ) {
                        state.unidades.forEach { unidad ->
                            DropdownMenuItem(
                                text = { Text(unidad.nombre) },
                                onClick = { viewModel.onUnidadSelected(unidad) }
                            )
                        }
                    }
                }

                if (state.isCatalogosLoading) {
                    FormSkeleton()
                } else {
                    state.catalogos?.let {
                        Card(modifier = Modifier.padding(top = 16.dp)) {
                            Column(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Registrar Movimiento de Stock",
                                    style = MaterialTheme.typography.titleLarge,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )
                                FormDropdown(
                                    items = it.especies,
                                    label = "Especie",
                                    selectedItem = state.selectedEspecie,
                                    onItemSelected = { viewModel.onEspecieSelected(it) },
                                    itemToString = { it.nombre },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                FormDropdown(
                                    items = state.filteredCategorias,
                                    label = "Categoría",
                                    selectedItem = state.selectedCategoria,
                                    onItemSelected = { viewModel.onCategoriaSelected(it) },
                                    itemToString = { it.nombre },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = state.selectedEspecie != null
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                FormDropdown(
                                    items = state.filteredRazas,
                                    label = "Raza",
                                    selectedItem = state.selectedRaza,
                                    onItemSelected = { viewModel.onRazaSelected(it) },
                                    itemToString = { it.nombre },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = state.selectedEspecie != null
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                FormDropdown(
                                    items = it.motivosMovimiento,
                                    label = "Motivo",
                                    selectedItem = state.selectedMotivo,
                                    onItemSelected = { viewModel.onMotivoSelected(it) },
                                    itemToString = { it.nombre },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = state.selectedEspecie != null
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                TextField(
                                    value = state.cantidad,
                                    onValueChange = { viewModel.onCantidadChanged(it) },
                                    label = { Text("Cantidad") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = state.selectedEspecie != null
                                )

                                val isDestinoVisible = state.selectedMotivo?.nombre?.contains("Traslado", ignoreCase = true) == true ||
                                        state.selectedMotivo?.nombre?.contains("Venta", ignoreCase = true) == true ||
                                        state.selectedMotivo?.nombre?.contains("Compra", ignoreCase = true) == true

                                if (isDestinoVisible) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    TextField(
                                        value = state.destino,
                                        onValueChange = { viewModel.onDestinoChanged(it) },
                                        label = { Text("Destino/Origen") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                Spacer(modifier = Modifier.height(32.dp))
                                if (state.isSaving) {
                                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                                } else {
                                    Button(
                                        onClick = { viewModel.saveMovement() },
                                        enabled = state.isFormValid,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Guardar")
                                    }
                                }

                                state.saveError?.let {
                                    Text(text = "Error al guardar: $it", color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
                                }
                                if (state.saveSuccess) {
                                    Text(text = "¡Movimiento guardado con éxito!", color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 8.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
