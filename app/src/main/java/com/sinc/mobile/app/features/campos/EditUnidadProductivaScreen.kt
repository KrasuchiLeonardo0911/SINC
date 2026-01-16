package com.sinc.mobile.app.features.campos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sinc.mobile.app.ui.components.MinimalHeader
import com.sinc.mobile.app.ui.components.SyncResultOverlay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditUnidadProductivaScreen(
    onNavigateBack: () -> Unit,
    unidadId: Int,
    viewModel: EditUnidadProductivaViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }
    
    // Handle Save Success
    if (uiState.saveSuccess) {
        SyncResultOverlay(
            show = true,
            message = "Campo actualizado correctamente",
            onDismiss = {
                onNavigateBack()
            }
        )
    }

    Scaffold(
        topBar = {
            MinimalHeader(
                title = "Editar Campo",
                onBackPress = onNavigateBack,
                modifier = Modifier
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                val unidad = uiState.unidad
                if (unidad != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Read-only fields
                        OutlinedTextField(
                            value = unidad.nombre ?: "Sin nombre",
                            onValueChange = {},
                            label = { Text("Nombre del Campo") },
                            enabled = false,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = unidad.identificadorLocal ?: "No disponible",
                            onValueChange = {},
                            label = { Text("RNSPA / Identificador") },
                            enabled = false,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Editable fields
                        OutlinedTextField(
                            value = uiState.superficie,
                            onValueChange = viewModel::onSuperficieChange,
                            label = { Text("Superficie (ha)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            isError = uiState.error != null && uiState.superficie.toDoubleOrNull() == null && uiState.superficie.isNotEmpty()
                        )

                        // Condicion Tenencia Dropdown
                        val condiciones = uiState.catalogos?.condicionesTenencia ?: emptyList()
                        DropdownField(
                            label = "Condición de Tenencia",
                            options = condiciones,
                            selectedOptionId = uiState.condicionTenenciaId,
                            onOptionSelected = { viewModel.onCondicionTenenciaChange(it.id) },
                            itemLabel = { it.nombre },
                            itemId = { it.id }
                        )

                        // Fuente Agua Dropdown
                        val fuentes = uiState.catalogos?.fuentesAgua ?: emptyList()
                         DropdownField(
                            label = "Fuente de Agua (Animales)",
                            options = fuentes,
                            selectedOptionId = uiState.fuenteAguaId,
                            onOptionSelected = { viewModel.onFuenteAguaChange(it.id) },
                            itemLabel = { it.nombre },
                            itemId = { it.id }
                         )
                        
                        OutlinedTextField(
                            value = uiState.observaciones,
                            onValueChange = viewModel::onObservacionesChange,
                            label = { Text("Observaciones") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = viewModel::saveChanges,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isSaving
                        ) {
                             if (uiState.isSaving) {
                                 CircularProgressIndicator(
                                     modifier = Modifier.size(24.dp),
                                     color = MaterialTheme.colorScheme.onPrimary,
                                     strokeWidth = 2.dp
                                 )
                                 Spacer(modifier = Modifier.size(8.dp))
                                 Text("Guardando...")
                             } else {
                                 Text("Guardar Cambios")
                             }
                        }
                    }
                } else {
                    Text(
                        text = "No se pudo cargar la información del campo.",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> DropdownField(
    label: String,
    options: List<T>,
    selectedOptionId: Int?,
    onOptionSelected: (T) -> Unit,
    itemLabel: (T) -> String,
    itemId: (T) -> Int
) {
    var expanded by remember { mutableStateOf(false) }
    
    val selectedOption = options.find { itemId(it) == selectedOptionId }
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedOption?.let(itemLabel) ?: "Seleccionar",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(text = itemLabel(option)) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}