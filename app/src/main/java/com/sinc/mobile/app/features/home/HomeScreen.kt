package com.sinc.mobile.app.features.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
    val state = viewModel.state.value

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            if (state.isLoading) {
                CircularProgressIndicator()
                return@item
            }

            state.error?.let {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Error: $it", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.error)
                    Button(onClick = { viewModel.loadInitialData() }, modifier = Modifier.padding(top = 8.dp)) {
                        Text("Reintentar")
                    }
                }
                return@item
            }

            // Step 1: Unidad Productiva Selection
            UnidadSelectionStep(
                unidades = state.unidades,
                selectedUnidad = state.selectedUnidad,
                isDropdownExpanded = state.isDropdownExpanded,
                onExpandedChange = viewModel::onDropdownExpandedChange,
                onUnidadSelected = viewModel::onUnidadSelected
            )

            if (state.selectedUnidad != null) {
                // Step 2: Action Selection (Alta/Baja)
                ActionSelectionStep(
                    onActionSelected = viewModel::onActionSelected,
                    modifier = Modifier.padding(top = 24.dp)
                )
            }
        }

        item {
            // Step 3: Movimiento Form (or Skeleton)
            if (state.selectedAction != null) {
                if (state.isSaving) {
                    FormSkeleton(modifier = Modifier.fillMaxWidth())
                } else {
                    MovimientoForm(
                        state = state,
                        viewModel = viewModel,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            }
        }

        // Always show pending movements if a UP is selected
        if (state.selectedUnidad != null && !state.isSaving) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text("Movimientos Pendientes", style = MaterialTheme.typography.titleLarge)
            }
            item {
                MovimientosPendientesTable(
                    movimientos = state.movimientosAgrupados, // Updated to use grouped data
                    catalogos = state.catalogos,
                    unidades = state.unidades,
                    onDelete = viewModel::deleteMovimientoGroup // Updated to call new delete function
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                SyncSection(state = state, onSync = viewModel::syncMovements)
            }
        }
    }
}

@Composable
private fun SyncSection(state: HomeState, onSync: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Button(
            onClick = onSync,
            enabled = state.movimientosAgrupados.isNotEmpty() && !state.isSyncing
        ) {
            if (state.isSyncing) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Sincronizando...")
                    Spacer(modifier = Modifier.width(8.dp))
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                }
            } else {
                Text("Sincronizar Cambios")
            }
        }

        if (state.syncSuccess) {
            Text(
                "Sincronización completada con éxito.",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        state.syncError?.let {
            Text(
                text = "Error: $it",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun MovimientosPendientesTable(
    movimientos: List<MovimientoAgrupado>, // Changed to grouped data model
    catalogos: com.sinc.mobile.domain.model.Catalogos?,
    unidades: List<com.sinc.mobile.domain.model.UnidadProductiva>,
    onDelete: (MovimientoAgrupado) -> Unit // Changed to grouped data model
) {
    if (movimientos.isEmpty()) {
        Text(
            "No hay movimientos pendientes.",
            modifier = Modifier.padding(top = 8.dp),
            style = MaterialTheme.typography.bodySmall
        )
        return
    }

    val columnWidths = listOf(150.dp, 100.dp, 150.dp, 120.dp, 150.dp, 50.dp, 50.dp)
    val totalTableWidth = columnWidths.sumOf { it.value.toDouble() }.dp
    val scrollState = rememberScrollState()

    Row(modifier = Modifier.horizontalScroll(scrollState)) {
        Column(
            modifier = Modifier
                .width(totalTableWidth)
                .border(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .height(IntrinsicSize.Min)
            ) {
                HeaderCell(text = "UP", width = columnWidths[0])
                VerticalDivider()
                HeaderCell(text = "Especie", width = columnWidths[1])
                VerticalDivider()
                HeaderCell(text = "Categoría", width = columnWidths[2])
                VerticalDivider()
                HeaderCell(text = "Raza", width = columnWidths[3])
                VerticalDivider()
                HeaderCell(text = "Motivo", width = columnWidths[4])
                VerticalDivider()
                HeaderCell(text = "Cant.", width = columnWidths[5])
                VerticalDivider()
                HeaderCell(text = "", width = columnWidths[6])
            }
            HorizontalDivider()

            // Data Rows
            movimientos.forEach { movimiento ->
                val up = unidades.find { it.id == movimiento.unidadProductivaId }?.nombre ?: "N/A"
                val especie = catalogos?.especies?.find { it.id == movimiento.especieId }?.nombre ?: "N/A"
                val categoria = catalogos?.categorias?.find { it.id == movimiento.categoriaId }?.nombre ?: "N/A"
                val raza = catalogos?.razas?.find { it.id == movimiento.razaId }?.nombre ?: "N/A"
                val motivo = catalogos?.motivosMovimiento?.find { it.id == movimiento.motivoMovimientoId }?.nombre ?: "N/A"

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DataCell(text = up, width = columnWidths[0])
                    VerticalDivider()
                    DataCell(text = especie, width = columnWidths[1])
                    VerticalDivider()
                    DataCell(text = categoria, width = columnWidths[2])
                    VerticalDivider()
                    DataCell(text = raza, width = columnWidths[3])
                    VerticalDivider()
                    DataCell(text = motivo, width = columnWidths[4])
                    VerticalDivider()
                    DataCell(text = movimiento.cantidadTotal.toString(), width = columnWidths[5]) // Use cantidadTotal
                    VerticalDivider()
                    Box(
                        modifier = Modifier
                            .width(columnWidths[6])
                            .fillMaxHeight()
                    ) {
                        IconButton(onClick = { onDelete(movimiento) }, modifier = Modifier.align(Alignment.Center)) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                        }
                    }
                }
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun HeaderCell(text: String, width: Dp) {
    Text(
        text = text,
        modifier = Modifier
            .width(width)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.bodyMedium
    )
}

@Composable
private fun DataCell(text: String, width: Dp) {
    Text(
        text = text,
        modifier = Modifier
            .width(width)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        style = MaterialTheme.typography.bodyMedium,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
private fun VerticalDivider() {
    Divider(
        modifier = Modifier
            .fillMaxHeight()
            .width(1.dp),
        color = MaterialTheme.colorScheme.outlineVariant
    )
}

@Composable
private fun HorizontalDivider() {
    Divider(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.outlineVariant
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UnidadSelectionStep(
    unidades: List<com.sinc.mobile.domain.model.UnidadProductiva>,
    selectedUnidad: com.sinc.mobile.domain.model.UnidadProductiva?,
    isDropdownExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onUnidadSelected: (com.sinc.mobile.domain.model.UnidadProductiva) -> Unit
) {
    Text(
        text = "Paso 1: Seleccione un campo",
        style = MaterialTheme.typography.headlineSmall,
        modifier = Modifier.padding(bottom = 16.dp)
    )
    ExposedDropdownMenuBox(
        expanded = isDropdownExpanded,
        onExpandedChange = onExpandedChange
    ) {
        TextField(
            value = selectedUnidad?.nombre ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Campo") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded)
            },
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = isDropdownExpanded,
            onDismissRequest = { onExpandedChange(false) }
        ) {
            unidades.forEach { unidad ->
                DropdownMenuItem(
                    text = { Text(unidad.nombre) },
                    onClick = { onUnidadSelected(unidad) }
                )
            }
        }
    }
}

@Composable
private fun ActionSelectionStep(
    onActionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Paso 2: Seleccione una acción",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(onClick = { onActionSelected("alta") }, modifier = Modifier.weight(1f)) {
                Text("Alta")
            }
            Button(onClick = { onActionSelected("baja") }, modifier = Modifier.weight(1f)) {
                Text("Baja")
            }
        }
    }
}

@Composable
private fun MovimientoForm(
    state: HomeState,
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier
) {
    state.catalogos?.let { catalogos ->
        Card(modifier = modifier) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Paso 3: Registrar ${state.selectedAction?.replaceFirstChar { it.uppercase() }}",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                FormDropdown(
                    items = catalogos.especies,
                    label = "Especie",
                    selectedItem = state.selectedEspecie,
                    onItemSelected = viewModel::onEspecieSelected,
                    itemToString = { especie -> especie.nombre },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                FormDropdown(
                    items = state.filteredCategorias,
                    label = "Categoría",
                    selectedItem = state.selectedCategoria,
                    onItemSelected = viewModel::onCategoriaSelected,
                    itemToString = { categoria -> categoria.nombre },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = state.selectedEspecie != null
                )
                Spacer(modifier = Modifier.height(16.dp))
                FormDropdown(
                    items = state.filteredRazas,
                    label = "Raza",
                    selectedItem = state.selectedRaza,
                    onItemSelected = viewModel::onRazaSelected,
                    itemToString = { raza -> raza.nombre },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = state.selectedEspecie != null
                )
                Spacer(modifier = Modifier.height(16.dp))
                FormDropdown(
                    items = state.filteredMotivos,
                    label = "Motivo",
                    selectedItem = state.selectedMotivo,
                    onItemSelected = viewModel::onMotivoSelected,
                    itemToString = { motivo -> motivo.nombre },
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
                Button(
                    onClick = { viewModel.saveMovement() },
                    enabled = state.isFormValid,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Guardar Movimiento")
                }

                state.saveError?.let {
                    Text(text = "Error al guardar: $it", color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
                }
            }
        }
    }
}