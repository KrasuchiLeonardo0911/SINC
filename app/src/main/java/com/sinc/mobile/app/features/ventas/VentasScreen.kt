package com.sinc.mobile.app.features.ventas

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.navigation.compose.hiltViewModel
import com.sinc.mobile.app.ui.components.*
import com.sinc.mobile.domain.model.Categoria
import com.sinc.mobile.domain.model.DeclaracionVenta
import com.sinc.mobile.domain.model.Especie
import com.sinc.mobile.domain.model.Raza
import com.sinc.mobile.domain.model.UnidadProductiva

@Composable
fun VentasScreen(
    onNavigateBack: () -> Unit,
    onNavigateToHistorial: () -> Unit,
    viewModel: VentasViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val tabs = listOf("Nueva Venta", "Seguimiento")
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    
    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }
    
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            MinimalHeader(
                title = "Declaración de Ventas",
                onBackPress = onNavigateBack,
                modifier = Modifier.statusBarsPadding(),
                actions = {
                    IconButton(onClick = onNavigateToHistorial) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Historial",
                            tint = Color.Black
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                tabs.forEachIndexed { index, title ->
                    val tabTitle = if (index == 1 && uiState.declaracionesActivas.isNotEmpty()) {
                        "$title (${uiState.declaracionesActivas.size})"
                    } else {
                        title
                    }

                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(tabTitle) }
                    )
                }
            }

            when (selectedTabIndex) {
                0 -> VentasForm(
                    uiState = uiState,
                    viewModel = viewModel
                )
                1 -> VentasList(
                    declaraciones = uiState.declaracionesActivas,
                    isLoading = uiState.isLoading,
                    onRefresh = { viewModel.onSyncRequested() },
                    onCancel = { id -> viewModel.onCancelDeclaracion(id) }
                )
            }
        }

        if (uiState.isLoading) {
            LoadingOverlay(isLoading = true)
        }
    }
}

sealed class VentasSheetContent {
    data class UPSheet(val items: List<UnidadProductiva>) : VentasSheetContent()
    data class RazaSheet(val items: List<Raza>) : VentasSheetContent()
    data class CategoriaSheet(val items: List<Categoria>) : VentasSheetContent()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VentasForm(
    uiState: VentasState,
    viewModel: VentasViewModel
) {
    var showSheet by remember { mutableStateOf(false) }
    var sheetContent by remember { mutableStateOf<VentasSheetContent?>(null) }

    if (showSheet && sheetContent != null) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            when (val content = sheetContent) {
                is VentasSheetContent.UPSheet -> {
                    SelectionSheetLayout(
                        title = "Seleccionar Campo",
                        items = content.items,
                        getItemName = { it.nombre ?: "Sin Nombre" },
                        onItemSelected = { 
                            viewModel.onUpSelected(it.id)
                            showSheet = false 
                        }
                    )
                }
                is VentasSheetContent.RazaSheet -> {
                    SelectionSheetLayout(
                        title = "Seleccionar Raza",
                        items = content.items,
                        getItemName = { it.nombre },
                        onItemSelected = { 
                            viewModel.onRazaSelected(it.id)
                            showSheet = false 
                        }
                    )
                }
                is VentasSheetContent.CategoriaSheet -> {
                    SelectionSheetLayout(
                        title = "Seleccionar Categoría",
                        items = content.items,
                        getItemName = { it.nombre },
                        onItemSelected = { 
                            viewModel.onCategoriaSelected(it.id)
                            showSheet = false 
                        }
                    )
                }
                else -> {}
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Título del Formulario
        item {
            Column {
                Text(
                    text = "Registrar Venta",
                    style = MaterialTheme.typography.headlineSmall.copy(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Seleccione los detalles de la venta.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
                )
            }
        }

        item {
            ClickableDropdownField(
                label = "Seleccionar un Campo",
                value = uiState.unidadesProductivas.find { it.id == uiState.selectedUpId }?.nombre,
                placeholder = "Seleccionar Campo",
                onClick = {
                    sheetContent = VentasSheetContent.UPSheet(uiState.unidadesProductivas)
                    showSheet = true
                }
            )
        }

        // Selector de Especie con Chips (Horizontal)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = "Especie", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(uiState.especies) { especie ->
                        val isSelected = uiState.selectedEspecieId == especie.id
                        VentasChip(
                            label = especie.nombre,
                            isSelected = isSelected,
                            onSelected = { viewModel.onEspecieSelected(especie.id) }
                        )
                    }
                }
            }
        }

        item {
            ClickableDropdownField(
                label = "Categoría",
                value = uiState.categorias.find { it.id == uiState.selectedCategoriaId }?.nombre,
                placeholder = "Seleccionar Categoría",
                enabled = uiState.selectedEspecieId != null,
                onClick = {
                    if (uiState.selectedEspecieId != null) {
                        sheetContent = VentasSheetContent.CategoriaSheet(uiState.categorias)
                        showSheet = true
                    }
                }
            )
        }

        item {
            ClickableDropdownField(
                label = "Raza",
                value = uiState.razas.find { it.id == uiState.selectedRazaId }?.nombre,
                placeholder = "Seleccionar Raza",
                enabled = uiState.selectedEspecieId != null,
                onClick = {
                    if (uiState.selectedEspecieId != null) {
                        sheetContent = VentasSheetContent.RazaSheet(uiState.razas)
                        showSheet = true
                    }
                }
            )
        }

        item {
            OutlinedTextField(
                value = uiState.pesoAproximado,
                onValueChange = { viewModel.onPesoAproximadoChanged(it) },
                label = { Text("Peso Vivo Aproximado (Kg) (Opcional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }

        // Cantidad con Stepper
        item {
            VentasQuantityStepper(
                cantidad = uiState.cantidad,
                onCantidadChanged = { viewModel.onCantidadChanged(it) },
                errorMessage = uiState.stockValidationMessage
            )
        }

        item {
            OutlinedTextField(
                value = uiState.observaciones,
                onValueChange = { viewModel.onObservacionesChanged(it) },
                label = { Text("Observaciones (Opcional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                shape = RoundedCornerShape(16.dp)
            )
        }

        item {
            Button(
                onClick = { viewModel.onSubmit() },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(25.dp),
                enabled = !uiState.isLoading
            ) {
                Text("Confirmar Venta", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
        
        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun VentasList(
    declaraciones: List<DeclaracionVenta>,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    onCancel: (Int) -> Unit
) {
    val pullRefreshState = rememberPullRefreshState(refreshing = isLoading, onRefresh = onRefresh)

    Box(modifier = Modifier.fillMaxSize().pullRefresh(pullRefreshState)) {
        if (declaraciones.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "No hay declaraciones en seguimiento.\nDeslice para actualizar.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = Color.Gray
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(declaraciones) { declaracion ->
                    DeclaracionCard(declaracion, onCancel)
                }
            }
        }
        
        PullRefreshIndicator(
            refreshing = isLoading,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

@Composable
fun DeclaracionCard(
    declaracion: DeclaracionVenta,
    onCancel: (Int) -> Unit
) {
    val statusUi = com.sinc.mobile.app.ui.util.LogisticaUiMapper.getStatusUi(declaracion.estado)
    var showCancelDialog by remember { mutableStateOf(false) }

    if (showCancelDialog) {
        com.sinc.mobile.app.ui.components.ConfirmationDialog(
            showDialog = true,
            title = "Cancelar Venta",
            message = "¿Está seguro de que desea cancelar esta declaración de venta? Esta acción no se puede deshacer.",
            onConfirm = {
                onCancel(declaracion.id)
                showCancelDialog = false
            },
            onDismiss = { showCancelDialog = false }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Cantidad y Estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${declaracion.cantidad} Animales",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    color = statusUi.containerColor,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        statusUi.icon?.let {
                            Icon(it, contentDescription = null, tint = statusUi.contentColor, modifier = Modifier.size(16.dp))
                        }
                        Text(
                            text = statusUi.label.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = statusUi.contentColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Stepper Simplificado
            if (statusUi.stepIndex >= 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Pasos: Comprometido -> Recogido -> Planta -> Final
                    // Dibujamos 4 pasos
                    for (i in 0..3) {
                        val isCompleted = i <= statusUi.stepIndex
                        val isCurrent = i == statusUi.stepIndex
                        
                        // Dot
                        Box(
                            modifier = Modifier
                                .size(if (isCurrent) 12.dp else 8.dp)
                                .background(
                                    color = if (isCompleted) statusUi.contentColor else Color.LightGray,
                                    shape = androidx.compose.foundation.shape.CircleShape
                                )
                        )
                        
                        // Line (excepto el último)
                        if (i < 3) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(2.dp)
                                    .background(if (i < statusUi.stepIndex) statusUi.contentColor else Color.LightGray.copy(alpha = 0.5f))
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(8.dp))
            
            // Detalles
            Text("Fecha Declaración: ${declaracion.fechaDeclaracion.take(10)}")
            
            declaracion.pesoAproximadoKg?.let { peso ->
                if (peso > 0) {
                    Text("Peso Vivo Aprox.: ${peso} Kg", style = MaterialTheme.typography.bodyMedium, color = Color.DarkGray)
                }
            }
            
            if (!declaracion.observaciones.isNullOrBlank()) {
                Text("Obs: ${declaracion.observaciones}", style = MaterialTheme.typography.bodyMedium, color = Color.DarkGray)
            }
            
            // Motivo Rechazo (si existe)
            if (!declaracion.motivoRechazo.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Motivo: ${declaracion.motivoRechazo}",
                        color = Color(0xFFC62828),
                        modifier = Modifier.padding(8.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // Botón Cancelar
            if (statusUi.canCancel) {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = { showCancelDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cancelar Venta")
                }
            }
        }
    }
}

// --- Componentes Visuales Estilo "Movimiento" ---

@Composable
fun VentasChip(label: String, isSelected: Boolean, onSelected: () -> Unit) {
    val containerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
    val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    val border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.primary)

    Card(
        modifier = Modifier.clip(RoundedCornerShape(50)).clickable { onSelected() },
        shape = RoundedCornerShape(50), // Fully rounded for chips
        colors = CardDefaults.cardColors(containerColor = containerColor, contentColor = contentColor),
        border = border
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp),
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 14.sp
        )
    }
}

@Composable
fun VentasQuantityStepper(
    cantidad: String,
    onCantidadChanged: (String) -> Unit,
    errorMessage: String? = null
) {
    val count = cantidad.toIntOrNull() ?: 0
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Cantidad", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Button Minus
                FilledIconButton(
                    onClick = { if (count > 0) onCantidadChanged((count - 1).toString()) },
                    enabled = count > 0,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        disabledContainerColor = Color.LightGray.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Menos")
                }

                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.widthIn(min = 40.dp),
                    textAlign = TextAlign.Center
                )

                // Button Plus
                FilledIconButton(
                    onClick = { onCantidadChanged((count + 1).toString()) },
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Más")
                }
            }
        }
        
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

@Composable
fun ClickableDropdownField(
    label: String,
    value: String?,
    placeholder: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = value ?: "",
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                enabled = false,
                placeholder = { Text(placeholder) },
                trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledContainerColor = Color.Transparent
                )
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable(enabled = enabled, onClick = onClick)
            )
        }
    }
}

@Composable
fun <T> SelectionSheetLayout(
    title: String,
    items: List<T>,
    getItemName: (T) -> String,
    onItemSelected: (T) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable { onItemSelected(item) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = getItemName(item),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        textAlign = TextAlign.Center,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}