package com.sinc.mobile.app.features.campos

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.sinc.mobile.app.ui.components.*
import com.sinc.mobile.app.ui.components.charts.ChartLegend
import com.sinc.mobile.app.ui.components.charts.LegendItem
import com.sinc.mobile.app.ui.components.charts.PieChart
import com.sinc.mobile.domain.model.*
import kotlinx.coroutines.launch

enum class EditSheetType {
    None, Suelo, Pasto, Tenencia, FuenteAguaAnimal, FuenteAguaHumano
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
    val scope = rememberCoroutineScope()

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var currentSheet by remember { mutableStateOf(EditSheetType.None) }
    
    // Internal navigation state
    var editingDistribution by remember { mutableStateOf(EditSheetType.None) }
    
    // State for editing items
    var editingItemPercentage by remember { mutableStateOf<Any?>(null) } 

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }

    if (uiState.saveSuccess) {
        if (editingDistribution != EditSheetType.None) {
            LaunchedEffect(Unit) {
                editingDistribution = EditSheetType.None
                viewModel.resetSaveSuccess()
            }
        } else {
            SyncResultOverlay(
                show = true,
                message = "Campo actualizado correctamente",
                onDismiss = { onNavigateBack() }
            )
        }
    }

    val hideSheet = {
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            if (!sheetState.isVisible) {
                currentSheet = EditSheetType.None
            }
        }
    }

    BackHandler(enabled = editingDistribution != EditSheetType.None) {
        editingDistribution = EditSheetType.None
    }

    AnimatedContent(
        targetState = editingDistribution,
        transitionSpec = {
            if (targetState != EditSheetType.None) {
                slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut()
            } else {
                slideInHorizontally { -it } + fadeIn() togetherWith slideOutHorizontally { it } + fadeOut()
            }
        },
        label = "screenTransition"
    ) { targetState ->
        when (targetState) {
            EditSheetType.None -> {
                // Main Form
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
                    Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                FormSectionCard("Información Básica") {
                                    uiState.unidad?.let {
                                        InfoRow(label = "Nombre", value = it.nombre ?: "Sin nombre")
                                        InfoRow(label = "RNSPA", value = it.identificadorLocal ?: "No disponible")
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = uiState.superficie,
                                        onValueChange = viewModel::onSuperficieChange,
                                        label = { Text("Superficie (ha)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.fillMaxWidth(),
                                        isError = uiState.superficie.isNotEmpty() && uiState.superficie.toDoubleOrNull() == null
                                    )
                                    HorizontalDivider()
                                    EditableRow(
                                        label = "Condición de Tenencia",
                                        value = uiState.catalogos?.condicionesTenencia?.find { it.id == uiState.condicionTenenciaId }?.nombre ?: "Seleccionar...",
                                        onClick = { currentSheet = EditSheetType.Tenencia }
                                    )
                                    HorizontalDivider()
                                    ToggleRow(
                                        label = "Habita en el campo",
                                        checked = uiState.habita,
                                        onCheckedChange = viewModel::onHabitaChange
                                    )
                                }

                                FormSectionCard("Agua") {
                                    Text("Consumo Humano", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    EditableRow(
                                        label = "Fuente de Agua",
                                        value = uiState.catalogos?.fuentesAgua?.find { it.id == uiState.aguaHumanoFuenteId }?.nombre ?: "Seleccionar...",
                                        onClick = { currentSheet = EditSheetType.FuenteAguaHumano }
                                    )
                                    ToggleRow(
                                        label = "Tiene agua en casa",
                                        checked = uiState.aguaHumanoEnCasa,
                                        onCheckedChange = viewModel::onAguaHumanoEnCasaChange
                                    )
                                    OutlinedTextField(
                                        value = uiState.aguaHumanoDistancia,
                                        onValueChange = viewModel::onAguaHumanoDistanciaChange,
                                        label = { Text("Distancia a Fuente de Agua (mts)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    HorizontalDivider()
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Consumo Animal", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    EditableRow(
                                        label = "Fuente de Agua",
                                        value = uiState.catalogos?.fuentesAgua?.find { it.id == uiState.aguaAnimalFuenteId }?.nombre ?: "Seleccionar...",
                                        onClick = { currentSheet = EditSheetType.FuenteAguaAnimal }
                                    )
                                    OutlinedTextField(
                                        value = uiState.aguaAnimalDistancia,
                                        onValueChange = viewModel::onAguaAnimalDistanciaChange,
                                        label = { Text("Distancia a Fuente de Agua (mts)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                DistributionSummaryCard(
                                    title = "Tipos de Suelo",
                                    items = uiState.tiposSuelo,
                                    getItemName = { (it as SueloInfo).nombre },
                                    getItemPercentage = { (it as SueloInfo).porcentaje.toFloat() },
                                    onEditClick = { editingDistribution = EditSheetType.Suelo }
                                )

                                DistributionSummaryCard(
                                    title = "Recursos Forrajeros",
                                    items = uiState.recursosForrajeros,
                                    getItemName = { (it as PastoInfo).nombre },
                                    getItemPercentage = { (it as PastoInfo).porcentaje?.toFloat() ?: 0f },
                                    onEditClick = { editingDistribution = EditSheetType.Pasto }
                                )

                                FormSectionCard("Observaciones") {
                                    OutlinedTextField(
                                        value = uiState.observaciones,
                                        onValueChange = viewModel::onObservacionesChange,
                                        label = { Text("Observaciones") },
                                        modifier = Modifier.fillMaxWidth(),
                                        minLines = 3
                                    )
                                }

                                Button(
                                    onClick = viewModel::saveChanges,
                                    modifier = Modifier.fillMaxWidth().height(50.dp),
                                    enabled = !uiState.isSaving,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                     if (uiState.isSaving) {
                                         CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                         Spacer(modifier = Modifier.width(8.dp))
                                         Text("Guardando...")
                                     } else {
                                         Text("Guardar Cambios")
                                     }
                                }
                            }
                        }
                    }
                }
            }
            EditSheetType.Suelo -> {
                DistributionEditorScreen(
                    title = "Editar Tipos de Suelo",
                    items = uiState.tiposSuelo,
                    getItemName = { it.nombre },
                    getItemPercentage = { it.porcentaje },
                    onConfirm = { viewModel.saveSuelos() },
                    onAddClick = {
                        currentSheet = EditSheetType.Suelo // Open modal to add
                    },
                    onEditPercentageClick = { item ->
                        editingItemPercentage = item
                    },
                    onDeleteItemClick = { viewModel.deleteSuelo(it.id) },
                    maxItems = 3
                )
            }
            EditSheetType.Pasto -> {
                DistributionEditorScreen(
                    title = "Editar Recursos Forrajeros",
                    items = uiState.recursosForrajeros,
                    getItemName = { it.nombre },
                    getItemPercentage = { it.porcentaje ?: 0 },
                    onConfirm = { viewModel.savePastos() },
                    onAddClick = {
                        currentSheet = EditSheetType.Pasto // Open modal to add
                    },
                    onEditPercentageClick = { item ->
                        editingItemPercentage = item
                    },
                    onDeleteItemClick = { viewModel.deletePasto(it.id) },
                    maxItems = 4
                )
            }
            else -> {}
        }
    }

    // Modal Bottom Sheet Logic
    if (currentSheet != EditSheetType.None) {
        ModalBottomSheet(
            onDismissRequest = hideSheet,
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            when (currentSheet) {
                EditSheetType.Suelo -> {
                    val existingIds = uiState.tiposSuelo.map { it.id }
                    SelectionSheetContent(
                        title = "Añadir Tipo de Suelo",
                        items = uiState.catalogos?.tiposSuelo?.filter { !existingIds.contains(it.id) } ?: emptyList(),
                        itemLabel = { it.nombre },
                        itemId = { it.id },
                        selectedId = null,
                        onSelect = { selected ->
                            viewModel.addOrUpdateSuelo(SueloInfo(selected.id, selected.nombre, 0))
                            hideSheet()
                        }
                    )
                }
                EditSheetType.Pasto -> {
                    val existingIds = uiState.recursosForrajeros.map { it.id }
                    SelectionSheetContent(
                        title = "Añadir Recurso Forrajero",
                        items = uiState.catalogos?.tiposPasto?.filter { !existingIds.contains(it.id) } ?: emptyList(),
                        itemLabel = { it.nombre },
                        itemId = { it.id },
                        selectedId = null,
                        onSelect = { selected ->
                            viewModel.addOrUpdatePasto(PastoInfo(selected.id, selected.nombre, 0))
                            hideSheet()
                        }
                    )
                }
                EditSheetType.Tenencia -> SelectionSheetContent(
                    title = "Condición de Tenencia",
                    items = uiState.catalogos?.condicionesTenencia ?: emptyList(),
                    itemLabel = { it.nombre },
                    itemId = { it.id },
                    selectedId = uiState.condicionTenenciaId,
                    onSelect = {
                        viewModel.onCondicionTenenciaChange(it.id)
                        hideSheet()
                    }
                )
                EditSheetType.FuenteAguaAnimal -> SelectionSheetContent(
                    title = "Fuente de Agua (Animal)",
                    items = uiState.catalogos?.fuentesAgua ?: emptyList(),
                    itemLabel = { it.nombre },
                    itemId = { it.id },
                    selectedId = uiState.aguaAnimalFuenteId,
                    onSelect = {
                        viewModel.onAguaAnimalFuenteChange(it.id)
                        hideSheet()
                    }
                )
                EditSheetType.FuenteAguaHumano -> SelectionSheetContent(
                    title = "Fuente de Agua (Humano)",
                    items = uiState.catalogos?.fuentesAgua ?: emptyList(),
                    itemLabel = { it.nombre },
                    itemId = { it.id },
                    selectedId = uiState.aguaHumanoFuenteId,
                    onSelect = {
                        viewModel.onAguaHumanoFuenteChange(it.id)
                        hideSheet()
                    }
                )
                else -> Spacer(Modifier.height(1.dp))
            }
        }
    }

    if (editingItemPercentage != null) {
        val item = editingItemPercentage
        val isSuelo = item is SueloInfo
        val initialPercentage = if (isSuelo) (item as SueloInfo).porcentaje else (item as PastoInfo).porcentaje ?: 0
        val name = if (isSuelo) (item as SueloInfo).nombre else (item as PastoInfo).nombre

        PercentageEditDialog(
            title = "Porcentaje para $name",
            initialPercentage = initialPercentage,
            onDismiss = { editingItemPercentage = null },
            onConfirm = { newPercentage ->
                if (isSuelo) {
                    val s = item as SueloInfo
                    viewModel.addOrUpdateSuelo(s.copy(porcentaje = newPercentage))
                } else {
                    val p = item as PastoInfo
                    viewModel.addOrUpdatePasto(p.copy(porcentaje = newPercentage))
                }
                editingItemPercentage = null
            }
        )
    }
}

// 1. DistributionSummaryCard
@Composable
fun <T> DistributionSummaryCard(
    title: String,
    items: List<T>,
    getItemName: (T) -> String,
    getItemPercentage: (T) -> Float,
    onEditClick: () -> Unit
) {
    val pieChartColors = listOf(Color(0xFF8C2218), Color(0xFF326B41), Color(0xFF2196F3), Color(0xFFFFC107))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
             Row(
                 modifier = Modifier.fillMaxWidth(),
                 verticalAlignment = Alignment.CenterVertically,
                 horizontalArrangement = Arrangement.SpaceBetween
             ) {
                 Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text("(Aproximado)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                
                IconButton(onClick = onEditClick) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary)
                }
            }
            
            Spacer(Modifier.height(8.dp))

            if (items.isEmpty()) {
                Text("No hay datos. Presione editar para añadir.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PieChart(
                        modifier = Modifier.size(100.dp),
                        values = items.map { getItemPercentage(it) },
                        colors = pieChartColors,
                    )
                    Spacer(Modifier.width(16.dp))
                    ChartLegend(
                        modifier = Modifier.fillMaxHeight(),
                        items = items.mapIndexed { index, item ->
                            LegendItem(
                                color = pieChartColors[index % pieChartColors.size],
                                text = getItemName(item),
                                percentage = getItemPercentage(item)
                            )
                        }
                    )
                }
            }
        }
    }
}

// 2. DistributionEditorScreen (Full Screen Editor)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> DistributionEditorScreen(
    title: String,
    items: List<T>,
    getItemName: (T) -> String,
    getItemPercentage: (T) -> Int,
    onConfirm: () -> Unit,
    onAddClick: () -> Unit,
    onEditPercentageClick: (T) -> Unit,
    onDeleteItemClick: (T) -> Unit,
    maxItems: Int
) {
    val currentSum = items.sumOf { getItemPercentage(it) }
    val isDistComplete = currentSum == 100
    val isValid = items.isEmpty() || items.size == 1 || isDistComplete

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    // No back arrow
                },
                modifier = Modifier.statusBarsPadding()
            )
        },
        floatingActionButton = {
            if (items.size < maxItems) {
                ExtendedFloatingActionButton(
                    onClick = onAddClick,
                    text = { Text("Añadir") },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            }
        },
        bottomBar = {
            Button(
                onClick = onConfirm,
                enabled = isValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding() 
                    .padding(16.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Guardar")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Validation Header
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isDistComplete) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
                ),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isDistComplete) Icons.Default.Check else Icons.Default.Edit, 
                        contentDescription = null,
                        tint = if (isDistComplete) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (isDistComplete) "Distribución completa (100%)" else "Suma actual: $currentSum% (Falta ${100 - currentSum}%)",
                        color = if (isDistComplete) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items) { item ->
                    PercentageListItem(
                        name = getItemName(item),
                        percentage = getItemPercentage(item),
                        onEditPercentageClick = { onEditPercentageClick(item) },
                        onDeleteClick = { onDeleteItemClick(item) }
                    )
                }
            }
        }
    }
}

@Composable
fun PercentageListItem(
    name: String,
    percentage: Int,
    onEditPercentageClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
            
            Row(
                modifier = Modifier
                    .clickable { onEditPercentageClick() }
                    .padding(vertical = 8.dp, horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("$percentage%", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(4.dp))
                Icon(Icons.Default.Edit, contentDescription = "Editar %", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
            }

            IconButton(onClick = onDeleteClick) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun PercentageEditDialog(
    title: String,
    initialPercentage: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var percentageStr by remember { mutableStateOf(initialPercentage.toString()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = percentageStr,
                    onValueChange = { if (it.all { char -> char.isDigit() }) percentageStr = it },
                    label = { Text("Porcentaje (%)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                Spacer(Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancelar") }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val p = percentageStr.toIntOrNull()
                            if (p != null && p in 1..100) {
                                onConfirm(p)
                            }
                        },
                        enabled = percentageStr.toIntOrNull() in 1..100
                    ) { Text("Aceptar") }
                }
            }
        }
    }
}

@Composable
fun <T> SelectionSheetContent(
    title: String,
    items: List<T>,
    itemLabel: (T) -> String,
    itemId: (T) -> Int,
    selectedId: Int?,
    onSelect: (T) -> Unit
) {
    Column(
        modifier = Modifier
            .navigationBarsPadding()
            .padding(16.dp)
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(items) { item ->
                val isSelected = itemId(item) == selectedId
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(item) },
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = itemLabel(item),
                        modifier = Modifier.padding(16.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}