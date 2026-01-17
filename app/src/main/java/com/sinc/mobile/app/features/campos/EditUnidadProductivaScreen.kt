package com.sinc.mobile.app.features.campos

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sinc.mobile.app.ui.components.MinimalHeader
import com.sinc.mobile.app.ui.components.SyncResultOverlay
import kotlinx.coroutines.launch

enum class EditSheetType {
    None,
    Tenencia,
    FuenteAguaHumano,
    FuenteAguaAnimal,
    TipoSuelo,
    TipoPasto
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditUnidadProductivaScreen(
    onNavigateBack: () -> Unit,
    unidadId: Int,
    viewModel: EditUnidadProductivaViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Bottom Sheet State
    val sheetState = rememberModalBottomSheetState()
    var currentSheet by remember { mutableStateOf(EditSheetType.None) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }
    
    if (uiState.saveSuccess) {
        SyncResultOverlay(
            show = true,
            message = "Campo actualizado correctamente",
            onDismiss = { onNavigateBack() }
        )
    }

    // Function to hide sheet
    val hideSheet = {
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            if (!sheetState.isVisible) {
                currentSheet = EditSheetType.None
            }
        }
    }

    Scaffold(
        topBar = {
            MinimalHeader(
                title = "Editar Campo",
                onBackPress = onNavigateBack,
                modifier = Modifier.statusBarsPadding()
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
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
                        // 1. Información Básica
                        InfoCard(title = "Información Básica") {
                            InfoRow(label = "Nombre", value = unidad.nombre ?: "Sin nombre")
                            InfoRow(label = "RNSPA", value = unidad.identificadorLocal ?: "No disponible")
                            InfoRow(label = "Coordenadas", value = String.format("%.4f, %.4f", unidad.latitud ?: 0.0, unidad.longitud ?: 0.0))
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            OutlinedTextField(
                                value = uiState.superficie,
                                onValueChange = viewModel::onSuperficieChange,
                                label = { Text("Superficie (ha)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                isError = uiState.error != null && uiState.superficie.toDoubleOrNull() == null
                            )

                            Divider()

                            EditableRow(
                                label = "Condición de Tenencia",
                                value = uiState.catalogos?.condicionesTenencia?.find { it.id == uiState.condicionTenenciaId }?.nombre ?: "Seleccionar...",
                                onClick = { currentSheet = EditSheetType.Tenencia }
                            )

                            Divider()

                            ToggleRow(
                                label = "¿Vive en el campo? (Habita)",
                                checked = uiState.habita,
                                onCheckedChange = viewModel::onHabitaChange
                            )
                        }

                        // 2. Agua
                        InfoCard(title = "Agua") {
                            Text(text = "Consumo Humano", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                            
                            EditableRow(
                                label = "Fuente de Agua",
                                value = uiState.catalogos?.fuentesAgua?.find { it.id == uiState.aguaHumanoFuenteId }?.nombre ?: "Seleccionar...",
                                onClick = { currentSheet = EditSheetType.FuenteAguaHumano }
                            )

                            ToggleRow(
                                label = "¿Tiene agua en casa?",
                                checked = uiState.aguaHumanoEnCasa,
                                onCheckedChange = viewModel::onAguaHumanoEnCasaChange
                            )

                            OutlinedTextField(
                                value = uiState.aguaHumanoDistancia,
                                onValueChange = viewModel::onAguaHumanoDistanciaChange,
                                label = { Text("Distancia (metros)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                enabled = !uiState.aguaHumanoEnCasa // Disable if water is in house
                            )

                            Spacer(modifier = Modifier.height(16.dp))
                            Divider()
                            Spacer(modifier = Modifier.height(8.dp))

                            Text(text = "Consumo Animal", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)

                            EditableRow(
                                label = "Fuente de Agua",
                                value = uiState.catalogos?.fuentesAgua?.find { it.id == uiState.aguaAnimalFuenteId }?.nombre ?: "Seleccionar...",
                                onClick = { currentSheet = EditSheetType.FuenteAguaAnimal }
                            )

                            OutlinedTextField(
                                value = uiState.aguaAnimalDistancia,
                                onValueChange = viewModel::onAguaAnimalDistanciaChange,
                                label = { Text("Distancia (metros)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }

                        // 3. Datos del Terreno
                        InfoCard(title = "Datos del Terreno") {
                            EditableRow(
                                label = "Tipo Suelo Predominante",
                                value = uiState.catalogos?.tiposSuelo?.find { it.id == uiState.tipoSueloId }?.nombre ?: "Seleccionar...",
                                onClick = { currentSheet = EditSheetType.TipoSuelo }
                            )
                            Divider()
                            
                            EditableRow(
                                label = "Tipo Pasto Predominante",
                                value = uiState.catalogos?.tiposPasto?.find { it.id == uiState.tipoPastoId }?.nombre ?: "Seleccionar...",
                                onClick = { currentSheet = EditSheetType.TipoPasto }
                            )
                            Divider()

                            ToggleRow(
                                label = "¿Forrajeras Predominantes?",
                                checked = uiState.forrajerasPredominante,
                                onCheckedChange = viewModel::onForrajerasChange
                            )
                        }

                        // 4. Observaciones
                        InfoCard(title = "Observaciones") {
                             OutlinedTextField(
                                value = uiState.observaciones,
                                onValueChange = viewModel::onObservacionesChange,
                                placeholder = { Text("Escriba aquí...") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 3,
                                shape = RoundedCornerShape(8.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = viewModel::saveChanges,
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            enabled = !uiState.isSaving,
                            shape = RoundedCornerShape(8.dp)
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
                }
            }
        }
        
        // Modal Bottom Sheet Implementation
        if (currentSheet != EditSheetType.None) {
            ModalBottomSheet(
                onDismissRequest = { currentSheet = EditSheetType.None },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                when (currentSheet) {
                    EditSheetType.Tenencia -> {
                        SelectionSheetContent(
                            title = "Seleccionar Condición de Tenencia",
                            items = uiState.catalogos?.condicionesTenencia ?: emptyList(),
                            selectedId = uiState.condicionTenenciaId,
                            itemLabel = { it.nombre },
                            itemId = { it.id },
                            onSelect = { 
                                viewModel.onCondicionTenenciaChange(it.id)
                                hideSheet()
                            }
                        )
                    }
                    EditSheetType.FuenteAguaHumano -> {
                        SelectionSheetContent(
                            title = "Seleccionar Fuente de Agua (Humano)",
                            items = uiState.catalogos?.fuentesAgua ?: emptyList(),
                            selectedId = uiState.aguaHumanoFuenteId,
                            itemLabel = { it.nombre },
                            itemId = { it.id },
                            onSelect = { 
                                viewModel.onAguaHumanoFuenteChange(it.id)
                                hideSheet()
                            }
                        )
                    }
                    EditSheetType.FuenteAguaAnimal -> {
                        SelectionSheetContent(
                            title = "Seleccionar Fuente de Agua (Animal)",
                            items = uiState.catalogos?.fuentesAgua ?: emptyList(),
                            selectedId = uiState.aguaAnimalFuenteId,
                            itemLabel = { it.nombre },
                            itemId = { it.id },
                            onSelect = { 
                                viewModel.onAguaAnimalFuenteChange(it.id)
                                hideSheet()
                            }
                        )
                    }
                    EditSheetType.TipoSuelo -> {
                        SelectionSheetContent(
                            title = "Seleccionar Tipo de Suelo",
                            items = uiState.catalogos?.tiposSuelo ?: emptyList(),
                            selectedId = uiState.tipoSueloId,
                            itemLabel = { it.nombre },
                            itemId = { it.id },
                            onSelect = { 
                                viewModel.onTipoSueloChange(it.id)
                                hideSheet()
                            }
                        )
                    }
                    EditSheetType.TipoPasto -> {
                        SelectionSheetContent(
                            title = "Seleccionar Tipo de Pasto",
                            items = uiState.catalogos?.tiposPasto ?: emptyList(),
                            selectedId = uiState.tipoPastoId,
                            itemLabel = { it.nombre },
                            itemId = { it.id },
                            onSelect = { 
                                viewModel.onTipoPastoChange(it.id)
                                hideSheet()
                            }
                        )
                    }
                    else -> {}
                }
                Spacer(modifier = Modifier.height(32.dp)) // Bottom padding
            }
        }
    }
}

// ... (Existing components InfoCard, InfoRow, EditableRow, Divider, ToggleRow) ...

@Composable
fun <T> SelectionSheetContent(
    title: String,
    items: List<T>,
    selectedId: Int?,
    itemLabel: (T) -> String,
    itemId: (T) -> Int,
    onSelect: (T) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items) { item ->
                val isSelected = itemId(item) == selectedId
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(item) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = itemLabel(item),
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Seleccionado",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InfoCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun EditableRow(label: String, value: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp)) // Apply rounded corners to the clickable area
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
        }
        Icon(
            imageVector = Icons.Outlined.Edit,
            contentDescription = "Editar",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun Divider() {
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
}

@Composable
fun ToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}