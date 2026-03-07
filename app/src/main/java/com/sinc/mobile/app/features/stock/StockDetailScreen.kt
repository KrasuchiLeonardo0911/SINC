package com.sinc.mobile.app.features.stock

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import com.sinc.mobile.app.features.stock.components.PieChart
import com.sinc.mobile.app.ui.components.MinimalHeader
import com.sinc.mobile.ui.theme.SincPrimary
import com.sinc.mobile.ui.theme.SincGrayBackground
import kotlin.math.roundToInt
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.navigation.NavHostController


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockDetailScreen(
    speciesName: String,
    grouping: String,
    unidadId: Int? = null,
    onBack: () -> Unit,
    navController: NavHostController,
    viewModel: StockViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Sincronizar la selección de unidad si viene por parámetro
    LaunchedEffect(unidadId) {
        if (unidadId != uiState.selectedUnidadId) {
            viewModel.selectUnidad(unidadId)
        }
    }

    // Estado local para el agrupamiento
    var currentGrouping by remember {
        mutableStateOf(
            try { StockGrouping.valueOf(grouping) } catch (e: Exception) { StockGrouping.BY_ALL }
        )
    }

    val detailData = remember(uiState.processedStock, speciesName, currentGrouping) {
        viewModel.getSpeciesDetailData(speciesName, currentGrouping)
    }

    // Estado para el modal de venta
    var showSaleSheet by remember { mutableStateOf(false) }
    var selectedItemForSale by remember { mutableStateOf<DesgloseItem.Full?>(null) }
    var pesoVenta by remember { mutableStateOf("") }
    var observacionesVenta by remember { mutableStateOf("") }
    var cantidadVenta by remember { mutableStateOf("1") }

    LaunchedEffect(uiState.saleSuccess, uiState.saleError) {
        if (uiState.saleSuccess != null || uiState.saleError != null) {
            showSaleSheet = false
            uiState.saleSuccess?.let { snackbarHostState.showSnackbar(it) }
            uiState.saleError?.let { snackbarHostState.showSnackbar(it) }
            viewModel.clearSaleMessages()
        }
    }

    val selectedUnidad = uiState.unidadesProductivas.find { it.id == uiState.selectedUnidadId }

    Scaffold(
        modifier = Modifier.fillMaxSize().navigationBarsPadding(),
        containerColor = Color.White,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            MinimalHeader(
                title = if (selectedUnidad != null) "Stock de Campo: ${selectedUnidad.nombre}" else "Stock General",
                onBackPress = onBack,
                modifier = Modifier.statusBarsPadding(),
                titleFontSize = 16.sp
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (detailData == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = SincPrimary)
                }
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Encabezado Formal Estilo Excel / Reporte (Franja Gris)
                    StockDetailTabHeader(
                        selectedGrouping = currentGrouping,
                        onGroupingSelected = { currentGrouping = it }
                    )

                    if (detailData.tableData.isEmpty() && uiState.selectedUnidadId != null) {
                        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text(
                                text = "No hay existencias de esta especie en el campo seleccionado.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(32.dp)
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(bottom = 24.dp)
                        ) {
                            // Lista Estilo Limpio
                            val listData = when(currentGrouping) {
                                StockGrouping.BY_ALL -> detailData.tableData.map { Triple(it.categoria, it.raza, it.quantity) to it }
                                StockGrouping.BY_CATEGORY -> detailData.legendItems.map { Triple(it.label, "", it.value) to null }
                                StockGrouping.BY_BREED -> detailData.legendItems.map { Triple(it.label, "", it.value) to null }
                            }

                            itemsIndexed(listData) { index, (data, originalItem) ->
                                val (title, subtitle, value) = data
                                StockListItem(
                                    title = title,
                                    subtitle = subtitle,
                                    value = value,
                                    isLast = index == listData.size - 1,
                                    onSellClick = if (currentGrouping == StockGrouping.BY_ALL && originalItem != null) {
                                        {
                                            if (uiState.selectedUnidadId != null) {
                                                selectedItemForSale = originalItem
                                                pesoVenta = ""
                                                observacionesVenta = ""
                                                cantidadVenta = "1"
                                                showSaleSheet = true
                                            } else {
                                                viewModel.viewModelScope.launch {
                                                    snackbarHostState.showSnackbar("Para vender, debe seleccionar un campo en la pantalla anterior.")
                                                }
                                            }
                                        }
                                    } else null
                                )
                            }

                            // Sección de Gráfico (Solo si hay agrupamiento específico) - AL FINAL
                            if (currentGrouping != StockGrouping.BY_ALL && detailData.chartData.isNotEmpty()) {
                                item {
                                    Spacer(modifier = Modifier.height(64.dp))
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(160.dp)
                                                .shadow(elevation = 4.dp, shape = CircleShape)
                                                .background(Color.White, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            PieChart(
                                                data = detailData.chartData,
                                                modifier = Modifier.fillMaxSize(),
                                                strokeWidth = 40f
                                            )
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(
                                                    text = detailData.stockTotal.toString(),
                                                    style = MaterialTheme.typography.headlineSmall,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = "Total",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Color.Gray
                                                )
                                            }
                                        }
                                        
                                        Spacer(modifier = Modifier.height(24.dp))
                                        
                                        // Leyenda del gráfico
                                        detailData.legendItems.forEach { item ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(10.dp)
                                                        .clip(CircleShape)
                                                        .background(item.color)
                                                )
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Text(
                                                    text = item.label,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                Text(
                                                    text = "${item.percentage.roundToInt()}%",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                        
                                        Spacer(modifier = Modifier.height(16.dp))
                                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal de Venta
    if (showSaleSheet && selectedItemForSale != null) {
        ModalBottomSheet(
            onDismissRequest = { showSaleSheet = false },
            sheetState = rememberModalBottomSheetState(),
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    text = "Registrar Venta",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "${selectedItemForSale?.categoria} - ${selectedItemForSale?.raza}",
                    style = MaterialTheme.typography.titleMedium,
                    color = SincPrimary
                )

                if (uiState.selectedUnidadId == null) {
                    Text(
                        text = "Para vender, debe seleccionar un campo arriba primero.",
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                } else {
                    OutlinedTextField(
                        value = pesoVenta,
                        onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) pesoVenta = it },
                        label = { Text("Peso Vivo Aproximado (Kg)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp)
                    )

                    QuantityStepper(
                        cantidad = cantidadVenta,
                        onCantidadChanged = { cantidadVenta = it }
                    )

                    OutlinedTextField(
                        value = observacionesVenta,
                        onValueChange = { observacionesVenta = it },
                        label = { Text("Observaciones (Opcional)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Button(
                        onClick = {
                            viewModel.onSellAnimal(
                                especieNombre = speciesName,
                                categoriaNombre = selectedItemForSale!!.categoria,
                                razaNombre = selectedItemForSale!!.raza,
                                cantidad = cantidadVenta.toIntOrNull() ?: 1,
                                peso = pesoVenta.toFloatOrNull(),
                                observaciones = observacionesVenta,
                                unidadId = uiState.selectedUnidadId!!
                            )
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(25.dp),
                        enabled = !uiState.isSelling
                    ) {
                        if (uiState.isSelling) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                        } else {
                            Text("Confirmar Venta")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun QuantityStepper(
    cantidad: String,
    onCantidadChanged: (String) -> Unit
) {
    val count = cantidad.toIntOrNull() ?: 0
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
            FilledIconButton(
                onClick = { if (count > 1) onCantidadChanged((count - 1).toString()) },
                enabled = count > 1,
                shape = RoundedCornerShape(12.dp),
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

            FilledIconButton(
                onClick = { onCantidadChanged((count + 1).toString()) },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Más")
            }
        }
    }
}

@Composable
private fun StockDetailTabHeader(
    selectedGrouping: StockGrouping,
    onGroupingSelected: (StockGrouping) -> Unit
) {
    val options = listOf(
        StockGrouping.BY_ALL to "TOTAL",
        StockGrouping.BY_CATEGORY to "CATEGORÍA",
        StockGrouping.BY_BREED to "RAZA"
    )
    
    val selectedIndex = options.indexOfFirst { it.first == selectedGrouping }

    Column(modifier = Modifier.fillMaxWidth().background(SincGrayBackground)) {
        TabRow(
            selectedTabIndex = selectedIndex,
            containerColor = SincGrayBackground,
            contentColor = SincPrimary,
            indicator = { tabPositions ->
                if (selectedIndex != -1) {
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedIndex]),
                        color = SincPrimary,
                        height = 2.dp
                    )
                }
            },
            divider = {} // Eliminamos el divisor por defecto para usar el nuestro más fino
        ) {
            options.forEachIndexed { index, (grouping, label) ->
                Tab(
                    selected = selectedIndex == index,
                    onClick = { onGroupingSelected(grouping) },
                    text = {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (selectedIndex == index) FontWeight.Bold else FontWeight.Medium,
                                letterSpacing = 0.5.sp
                            ),
                            color = if (selectedIndex == index) SincPrimary else Color.Gray,
                            maxLines = 1
                        )
                    }
                )
            }
        }
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
        )
    }
}

@Composable
private fun StockListItem(
    title: String,
    subtitle: String,
    value: Int,
    isLast: Boolean,
    onSellClick: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                if (subtitle.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = value.toString(),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = SincPrimary
                    ),
                    modifier = Modifier.padding(end = if (onSellClick != null) 16.dp else 0.dp)
                )
                
                if (onSellClick != null) {
                    IconButton(
                        onClick = onSellClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "Vender",
                            tint = Color(0xFF2E7D32), // Verde oscuro
                            modifier = Modifier.size(24.dp) // Aumentamos un poco el tamaño del icono ya que no tiene fondo
                        )
                    }
                }
            }
        }
    }
    
    if (!isLast) {
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 24.dp),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
        )
    }
}
