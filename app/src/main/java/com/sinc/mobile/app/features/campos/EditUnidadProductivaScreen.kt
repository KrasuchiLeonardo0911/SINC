package com.sinc.mobile.app.features.campos

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.sinc.mobile.app.ui.components.*
import com.sinc.mobile.app.ui.components.charts.ChartLegend
import com.sinc.mobile.app.ui.components.charts.LegendItem
import com.sinc.mobile.app.ui.components.charts.PieChart
import com.sinc.mobile.domain.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class EditSheetType {
    None, Suelo, Pasto, Tenencia, FuenteAguaAnimal, FuenteAguaHumano, BasicInfo, WaterInfo, Observations
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
    
    var editingDistribution by remember { mutableStateOf(EditSheetType.None) }
    var editingItemPercentage by remember { mutableStateOf<Any?>(null) } 
    var editingItemType by remember { mutableStateOf<Any?>(null) }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }

    val hideSheet = {
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            if (!sheetState.isVisible) {
                currentSheet = EditSheetType.None
                editingItemType = null
            }
        }
    }

    BackHandler(enabled = editingDistribution != EditSheetType.None) {
        editingDistribution = EditSheetType.None
    }

    Box(modifier = Modifier.fillMaxSize()) {
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
                                title = "Información del Campo",
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
                                    EditableSectionCard(
                                        title = "Información Básica",
                                        onEditClick = { editingDistribution = EditSheetType.BasicInfo }
                                    ) {
                                        uiState.unidad?.let {
                                            InfoRow(label = "Nombre", value = it.nombre ?: "Sin nombre")
                                            InfoRow(label = "RNSPA", value = it.identificadorLocal ?: "No disponible")
                                        }
                                        InfoRow(
                                            label = "Superficie",
                                            value = if (uiState.superficie.isNotEmpty()) "${uiState.superficie} ha" else "No definida"
                                        )
                                        InfoRow(
                                            label = "Condición de Tenencia",
                                            value = uiState.catalogos?.condicionesTenencia?.find { it.id == uiState.condicionTenenciaId }?.nombre ?: "Sin definir"
                                        )
                                        InfoRow(
                                            label = "¿Vive en el campo?",
                                            value = if (uiState.habita) "Sí" else "No"
                                        )
                                    }

                                    EditableSectionCard(
                                        title = "Agua",
                                        onEditClick = { editingDistribution = EditSheetType.WaterInfo }
                                    ) {
                                        Text("Consumo Humano", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        InfoRow(
                                            label = "Fuente de Agua",
                                            value = uiState.catalogos?.fuentesAgua?.find { it.id == uiState.aguaHumanoFuenteId }?.nombre ?: "Sin definir"
                                        )
                                        InfoRow(
                                            label = "¿Tiene agua en casa?",
                                            value = if (uiState.aguaHumanoEnCasa) "Sí" else "No"
                                        )
                                        if (!uiState.aguaHumanoEnCasa) {
                                            InfoRow(
                                                label = "Distancia (mts)",
                                                value = if (uiState.aguaHumanoDistancia.isNotEmpty()) uiState.aguaHumanoDistancia else "-"
                                            )
                                        }
                                        
                                        Spacer(modifier = Modifier.height(8.dp))
                                        HorizontalDivider()
                                        Spacer(modifier = Modifier.height(8.dp))
                                        
                                        Text("Consumo Animal", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        InfoRow(
                                            label = "Fuente de Agua",
                                            value = uiState.catalogos?.fuentesAgua?.find { it.id == uiState.aguaAnimalFuenteId }?.nombre ?: "Sin definir"
                                        )
                                        InfoRow(
                                            label = "Distancia (mts)",
                                            value = if (uiState.aguaAnimalDistancia.isNotEmpty()) uiState.aguaAnimalDistancia else "-"
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

                                    EditableSectionCard(
                                        title = "Observaciones",
                                        onEditClick = { editingDistribution = EditSheetType.Observations }
                                    ) {
                                        OutlinedTextField(
                                            value = uiState.observaciones,
                                            onValueChange = viewModel::onObservacionesChange,
                                            label = { Text("Observaciones") },
                                            modifier = Modifier.fillMaxWidth(),
                                            enabled = false, // Static info
                                            minLines = 3
                                        )
                                    }

                                    Button(
                                        onClick = viewModel::saveChanges,
                                        modifier = Modifier.fillMaxWidth().height(50.dp),
                                        enabled = false, // Disabled as requested
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
                EditSheetType.BasicInfo -> {
                    BasicInfoEditorScreen(
                        uiState = uiState,
                        onBack = { editingDistribution = EditSheetType.None },
                        onSave = { viewModel.saveBasicInfo() },
                        onSuperficieChange = viewModel::onSuperficieChange,
                        onTenenciaClick = { currentSheet = EditSheetType.Tenencia },
                        onHabitaChange = viewModel::onHabitaChange
                    )
                }
                EditSheetType.WaterInfo -> {
                    WaterInfoEditorScreen(
                        uiState = uiState,
                        onBack = { editingDistribution = EditSheetType.None },
                        onSave = { viewModel.saveWaterInfo() },
                        onAguaHumanoFuenteClick = { currentSheet = EditSheetType.FuenteAguaHumano },
                        onAguaHumanoEnCasaChange = viewModel::onAguaHumanoEnCasaChange,
                        onAguaHumanoDistanciaChange = viewModel::onAguaHumanoDistanciaChange,
                        onAguaAnimalFuenteClick = { currentSheet = EditSheetType.FuenteAguaAnimal },
                        onAguaAnimalDistanciaChange = viewModel::onAguaAnimalDistanciaChange
                    )
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
                        maxItems = 3,
                        onBack = { editingDistribution = EditSheetType.None }
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
                        maxItems = 4,
                        onBack = { editingDistribution = EditSheetType.None }
                    )
                }
                EditSheetType.Observations -> {
                    ObservationsEditorScreen(
                        uiState = uiState,
                        onBack = { editingDistribution = EditSheetType.None },
                        onSave = { viewModel.saveObservations() },
                        onObservacionesChange = viewModel::onObservacionesChange
                    )
                }
                else -> {}
            }
        }

        // Full Screen Loading/Success Overlay
        if (uiState.isSaving || uiState.saveSuccess) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(alpha = 1f) // Ensure opaque
                    .background(Color.White)
                    .zIndex(99f) // Top most
                    .clickable(enabled = false) {}, // Block clicks
                contentAlignment = Alignment.Center
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(60.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 4.dp
                    )
                } else {
                    // Success State
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Éxito",
                            tint = Color(0xFF4CAF50), // Green success
                            modifier = Modifier.size(80.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Guardado correctamente",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )

                        // Manual close button inside the column
                        Button(
                            onClick = {
                                scope.launch {
                                    if (editingDistribution != EditSheetType.None) {
                                        editingDistribution = EditSheetType.None
                                        delay(500) // Wait for background slide
                                    }
                                    viewModel.resetSaveSuccess()
                                }
                            },
                            modifier = Modifier.padding(top = 32.dp)
                        ) {
                            Text("Volver")
                        }
                    }
                }
            }
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

// 1. EditableSectionCard
@Composable
fun EditableSectionCard(
    title: String,
    onEditClick: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
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
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                IconButton(onClick = onEditClick) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

// 2. BasicInfoEditorScreen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BasicInfoEditorScreen(
    uiState: EditUnidadProductivaState,
    onBack: () -> Unit,
    onSave: () -> Unit,
    onSuperficieChange: (String) -> Unit,
    onTenenciaClick: () -> Unit,
    onHabitaChange: (Boolean) -> Unit
) {
    Scaffold(
        topBar = {
            MinimalHeader(
                title = "Editar Información Básica",
                onBackPress = onBack,
                modifier = Modifier.statusBarsPadding()
            )
        },
        bottomBar = {
            Button(
                onClick = onSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(16.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Guardar")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = uiState.superficie,
                onValueChange = onSuperficieChange,
                label = { Text("Superficie (ha)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                isError = uiState.superficie.isNotEmpty() && uiState.superficie.toDoubleOrNull() == null
            )
            
            EditableRow(
                label = "Condición de Tenencia",
                value = uiState.catalogos?.condicionesTenencia?.find { it.id == uiState.condicionTenenciaId }?.nombre ?: "Seleccionar...",
                onClick = onTenenciaClick
            )
            
            HorizontalDivider()
            
            ToggleRow(
                label = "Habita en el campo",
                checked = uiState.habita,
                onCheckedChange = onHabitaChange
            )
        }
    }
}

// 3. WaterInfoEditorScreen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaterInfoEditorScreen(
    uiState: EditUnidadProductivaState,
    onBack: () -> Unit,
    onSave: () -> Unit,
    onAguaHumanoFuenteClick: () -> Unit,
    onAguaHumanoEnCasaChange: (Boolean) -> Unit,
    onAguaHumanoDistanciaChange: (String) -> Unit,
    onAguaAnimalFuenteClick: () -> Unit,
    onAguaAnimalDistanciaChange: (String) -> Unit
) {
    Scaffold(
        topBar = {
            MinimalHeader(
                title = "Editar Agua",
                onBackPress = onBack,
                modifier = Modifier.statusBarsPadding()
            )
        },
        bottomBar = {
            Button(
                onClick = onSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(16.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Guardar")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Consumo Humano", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            
            EditableRow(
                label = "Fuente de Agua",
                value = uiState.catalogos?.fuentesAgua?.find { it.id == uiState.aguaHumanoFuenteId }?.nombre ?: "Seleccionar...",
                onClick = onAguaHumanoFuenteClick
            )
            
            ToggleRow(
                label = "Tiene agua en casa",
                checked = uiState.aguaHumanoEnCasa,
                onCheckedChange = onAguaHumanoEnCasaChange
            )
            
            OutlinedTextField(
                value = uiState.aguaHumanoDistancia,
                onValueChange = onAguaHumanoDistanciaChange,
                label = { Text("Distancia a Fuente de Agua (mts)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.aguaHumanoEnCasa
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))
            
            Text("Consumo Animal", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            
            EditableRow(
                label = "Fuente de Agua",
                value = uiState.catalogos?.fuentesAgua?.find { it.id == uiState.aguaAnimalFuenteId }?.nombre ?: "Seleccionar...",
                onClick = onAguaAnimalFuenteClick
            )
            
            OutlinedTextField(
                value = uiState.aguaAnimalDistancia,
                onValueChange = onAguaAnimalDistanciaChange,
                label = { Text("Distancia a Fuente de Agua (mts)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// 4. ObservationsEditorScreen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ObservationsEditorScreen(
    uiState: EditUnidadProductivaState,
    onBack: () -> Unit,
    onSave: () -> Unit,
    onObservacionesChange: (String) -> Unit
) {
    Scaffold(
        topBar = {
            MinimalHeader(
                title = "Editar Observaciones",
                onBackPress = onBack,
                modifier = Modifier.statusBarsPadding()
            )
        },
        bottomBar = {
            Button(
                onClick = onSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(16.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Guardar")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            val lineHeight = 30.dp
            
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val lineSpacing = lineHeight.toPx()
                
                var y = lineSpacing
                while (y < canvasHeight) {
                    drawLine(
                        start = Offset(0f, y),
                        end = Offset(canvasWidth, y),
                        color = Color.LightGray.copy(alpha = 0.5f),
                        strokeWidth = 1.dp.toPx()
                    )
                    y += lineSpacing
                }
            }

            BasicTextField(
                value = uiState.observaciones,
                onValueChange = onObservacionesChange,
                modifier = Modifier.fillMaxSize(),
                textStyle = TextStyle(
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    lineHeight = 30.sp // Match canvas lines roughly
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
            )
            
            if (uiState.observaciones.isEmpty()) {
                Text(
                    "Escriba sus observaciones aquí...",
                    style = TextStyle(fontSize = 18.sp, color = Color.Gray),
                    modifier = Modifier.padding(top = 2.dp) // Slight adjust
                )
            }
        }
    }
}

// DistributionSummaryCard
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

// DistributionEditorScreen (Full Screen Editor)
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
    maxItems: Int,
    onBack: () -> Unit // Used by MinimalHeader
) {
    val currentSum = items.sumOf { getItemPercentage(it) }
    val hasZeroValues = items.any { getItemPercentage(it) == 0 }
    val isDistComplete = currentSum == 100 && !hasZeroValues
    
    val isValid = items.isEmpty() || isDistComplete

    Scaffold(
        topBar = {
            MinimalHeader(
                title = title,
                onBackPress = onBack,
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
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Validation Header
            val (bgColor, contentColor, icon, text) = if (items.isEmpty()) {
                Quadruple(
                    MaterialTheme.colorScheme.surfaceVariant,
                    MaterialTheme.colorScheme.onSurfaceVariant,
                    Icons.Default.Delete,
                    "Sin datos. Se borrará la distribución."
                )
            } else if (hasZeroValues) {
                Quadruple(
                    MaterialTheme.colorScheme.errorContainer,
                    MaterialTheme.colorScheme.onErrorContainer,
                    Icons.Default.Warning,
                    "Error: Hay elementos con 0%"
                )
            } else if (!isDistComplete) {
                Quadruple(
                    MaterialTheme.colorScheme.errorContainer,
                    MaterialTheme.colorScheme.onErrorContainer,
                    Icons.Default.Close,
                    "Suma actual: $currentSum% (Falta ${100 - currentSum}%)"
                )
            } else {
                Quadruple(
                    Color(0xFFE8F5E9),
                    Color(0xFF1B5E20),
                    Icons.Default.Check,
                    "Distribución completa (100%)"
                )
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = bgColor),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon, 
                        contentDescription = null,
                        tint = contentColor
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = text,
                        color = contentColor,
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

data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

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
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 20.dp)
        )
        
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(items) { item ->
                val isSelected = itemId(item) == selectedId
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(item) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                    border = BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = itemLabel(item),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                        
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Seleccionado",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}