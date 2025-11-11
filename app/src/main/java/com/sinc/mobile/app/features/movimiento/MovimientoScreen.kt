package com.sinc.mobile.app.features.movimiento

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.clickable
import kotlinx.coroutines.launch
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
import com.sinc.mobile.ui.theme.SuccessGreen
import androidx.compose.ui.graphics.Color
import com.sinc.mobile.app.ui.components.FormDropdown
import com.sinc.mobile.app.ui.components.FormSkeleton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovimientoScreen(viewModel: MovimientoViewModel = hiltViewModel()) {
    val state = viewModel.state.value
    val scaffoldState = rememberBottomSheetScaffoldState()
    val scope = rememberCoroutineScope()

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 56.dp, // Altura de la pestaña cuando está colapsada
        sheetContent = {
            // Contenido de la hoja inferior (Movimientos Pendientes)
            MovimientosPendientesSheetContent(
                state = state,
                viewModel = viewModel,
                onHeaderClick = {
                    scope.launch {
                        scaffoldState.bottomSheetState.expand()
                    }
                }
            )
        }
    ) { innerPadding ->
        // Contenido principal de la pantalla (Pasos 1, 2, 3)
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding), // Padding del Scaffold
            contentPadding = PaddingValues(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp) // Espacio entre items
        ) {
            item { Spacer(modifier = Modifier.height(16.dp)) } // Espacio superior
            item {
                if (state.isLoading) {
                    CircularProgressIndicator()
                    return@item
                }

                state.error?.let {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "Error: $it", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { viewModel.loadInitialData() }) {
                                Text("Reintentar")
                            }
                        }
                    }
                    return@item
                }

                // Step 1: Unidad Productiva Selection in a Card
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        UnidadSelectionStep(
                            unidades = state.unidades,
                            selectedUnidad = state.selectedUnidad,
                            isDropdownExpanded = state.isDropdownExpanded,
                            onExpandedChange = viewModel::onDropdownExpandedChange,
                            onUnidadSelected = viewModel::onUnidadSelected
                        )
                    }
                }
            }

            if (state.selectedUnidad != null) {
                item {
                    // Step 2: Action Selection in a Card
                    Card(modifier = Modifier.fillMaxWidth()) {
                        ActionSelectionStep(
                            onActionSelected = viewModel::onActionSelected,
                            modifier = Modifier.padding(16.dp),
                            selectedAction = state.selectedAction
                        )
                    }
                }
            }

            // Step 3: Movimiento Form (or Skeleton)
            if (state.selectedAction != null) {
                item {
                    if (state.isFormLoading || state.isSaving) {
                        FormSkeleton(modifier = Modifier.fillMaxWidth())
                    } else {
                        MovimientoForm(
                            state = state,
                            viewModel = viewModel
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MovimientosPendientesSheetContent(
    state: MovimientoState,
    viewModel: MovimientoViewModel,
    onHeaderClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Pestaña para expandir
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clickable(onClick = onHeaderClick),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text("Movimientos Pendientes (${state.movimientosAgrupados.size})")
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Expandir")
        }

        // Contenido de la tabla y sincronización
        MovimientosPendientesTable(
            movimientos = state.movimientosAgrupados,
            catalogos = state.catalogos,
            unidades = state.unidades,
            onDelete = viewModel::deleteMovimientoGroup
        )
        Spacer(modifier = Modifier.height(24.dp))
        SyncSection(state = state, onSync = viewModel::syncMovements)
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun SyncSection(state: MovimientoState, onSync: () -> Unit) {
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
    movimientos: List<MovimientoAgrupado>,
    catalogos: com.sinc.mobile.domain.model.Catalogos?,
    unidades: List<com.sinc.mobile.domain.model.UnidadProductiva>,
    onDelete: (MovimientoAgrupado) -> Unit
) {
    if (movimientos.isEmpty()) {
        Text(
            "No hay movimientos pendientes.",
            modifier = Modifier.padding(top = 8.dp),
            style = MaterialTheme.typography.bodyMedium
        )
        return
    }

    // Usamos un Column en lugar de LazyColumn porque ya estamos dentro de una LazyColumn padre
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        movimientos.forEach { movimiento ->
            MovimientoItemCard(movimiento, catalogos, unidades, onDelete)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MovimientoItemCard(
    movimiento: MovimientoAgrupado,
    catalogos: com.sinc.mobile.domain.model.Catalogos?,
    unidades: List<com.sinc.mobile.domain.model.UnidadProductiva>,
    onDelete: (MovimientoAgrupado) -> Unit
) {
    val motivo = catalogos?.motivosMovimiento?.find { it.id == movimiento.motivoMovimientoId }?.nombre ?: "N/A"
    val isAlta = motivo.contains("Alta", ignoreCase = true) || motivo.contains("Compra", ignoreCase = true)

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono y Cantidad
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val (text, color) = if (isAlta) {
                    "+" to SuccessGreen
                } else {
                    "" to MaterialTheme.colorScheme.error // No hay texto para la baja, solo color
                }
                Text(
                    text = text,
                    style = MaterialTheme.typography.headlineMedium,
                    color = color
                )
                Text(
                    text = movimiento.cantidadTotal.toString(),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = color // Aplicamos el color también al número
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Detalles del Movimiento
            Column(modifier = Modifier.weight(1f)) {
                val especie = catalogos?.especies?.find { it.id == movimiento.especieId }?.nombre ?: "N/A"
                val categoria = catalogos?.categorias?.find { it.id == movimiento.categoriaId }?.nombre ?: "N/A"

                Text(text = motivo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    text = "$especie | $categoria",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                val up = unidades.find { it.id == movimiento.unidadProductivaId }?.nombre ?: "N/A"
                Text(
                    text = up,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            // Botón de Borrar
            IconButton(onClick = { onDelete(movimiento) }) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar")
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
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    )
}

@Composable
private fun HorizontalDivider() {
    Divider(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
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
    // Texto del título más sutil
    Text(
        text = "Seleccione el campo",
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(bottom = 8.dp) // Reducimos el padding inferior
    )
    ExposedDropdownMenuBox(
        expanded = isDropdownExpanded,
        onExpandedChange = onExpandedChange
    ) {
        // Usamos OutlinedTextField para un look más moderno
        OutlinedTextField(
            value = selectedUnidad?.nombre ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Campo") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded)
            },
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
    modifier: Modifier = Modifier,
    selectedAction: String? // Nuevo parámetro
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Seleccione una acción",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            val isAltaSelected = selectedAction == "alta"
            val isBajaSelected = selectedAction == "baja"

            OutlinedButton(
                onClick = { onActionSelected("alta") },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (isAltaSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    contentColor = if (isAltaSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                ),
                border = if (isAltaSelected) null else ButtonDefaults.outlinedButtonBorder
            ) {
                Text("Alta")
            }
            OutlinedButton(
                onClick = { onActionSelected("baja") },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (isBajaSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    contentColor = if (isBajaSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                ),
                border = if (isBajaSelected) null else ButtonDefaults.outlinedButtonBorder
            ) {
                Text("Baja")
            }
        }
    }
}

@Composable
private fun MovimientoForm(
    state: MovimientoState,
    viewModel: MovimientoViewModel,
    modifier: Modifier = Modifier
) {
    state.catalogos?.let {
        // Esta Card ya existe, así que el estilo se aplicará automáticamente.
        Card(modifier = modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Registrar ${state.selectedAction?.replaceFirstChar { it.uppercase() }}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                FormDropdown(
                    items = it.especies,
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
                OutlinedTextField(
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
                    OutlinedTextField(
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