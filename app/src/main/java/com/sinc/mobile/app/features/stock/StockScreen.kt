package com.sinc.mobile.app.features.stock

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sinc.mobile.app.features.stock.components.GroupingOptions
import com.sinc.mobile.app.features.stock.components.LegendItem
import com.sinc.mobile.app.features.stock.components.PieChart
import com.sinc.mobile.app.features.stock.components.PieChartData
import com.sinc.mobile.app.ui.components.MinimalHeader
import com.sinc.mobile.ui.theme.SincPrimary
import com.sinc.mobile.domain.model.UnidadProductiva

// Paleta de colores local para gráficos dinámicos en la UI
private val uiPieChartColors = listOf(
    Color(0xFF2E7D32), // Verde Fuerte
    Color(0xFF66BB6A), // Verde Claro
    Color(0xFF9CCC65), // Verde Lima Suave
    Color(0xFF26A69A), // Verde Azulado
    Color(0xFFFFA726), // Naranja (Contraste)
    Color(0xFF29B6F6), // Azul Claro (Contraste)
    Color(0xFF78909C), // Gris Azulado
    Color(0xFF8D6E63)  // Marrón Suave
)

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun StockScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onNavigateToVentas: () -> Unit,
    mainScaffoldBottomPadding: Dp,
    viewModel: StockViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val processedStock = uiState.processedStock

    // Fondo gris del MainScreen
    val backgroundColor = Color(0xFFF5F7FA)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = backgroundColor,
        topBar = {
            MinimalHeader(
                title = "Mi Stock",
                onBackPress = onBack,
                modifier = Modifier.statusBarsPadding(),
                actions = {
                    TextButton(
                        onClick = onNavigateToVentas,
                        colors = ButtonDefaults.textButtonColors(contentColor = SincPrimary)
                    ) {
                        Text(
                            text = "Vender Stock",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isInitialLoad) {
            Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(bottom = mainScaffoldBottomPadding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        } else {
            val pullRefreshState = rememberPullRefreshState(refreshing = uiState.isLoading, onRefresh = viewModel::refresh)

            Box(
                Modifier
                    .pullRefresh(pullRefreshState)
                    .padding(paddingValues)
                    .padding(bottom = mainScaffoldBottomPadding)
                    .fillMaxSize()
            ) {
                if (processedStock != null) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        // Padding vertical solamente, horizontal es 0 para el estilo "full width"
                        contentPadding = PaddingValues(top = 0.dp, bottom = 16.dp), 
                        verticalArrangement = Arrangement.spacedBy(8.dp) // Separación gris entre secciones
                    ) {
                        
                        // Sección de Filtros
                        item {
                            StockFilterSection(
                                unidades = uiState.unidadesProductivas,
                                selectedUnidadId = uiState.selectedUnidadId,
                                onSelectUnidad = viewModel::selectUnidad
                            )
                        }

                        // Sección de Stock Total
                        item {
                            TotalStockSection(processedStock)
                        }

                        // Secciones de Especies
                        items(processedStock.allSpecies) { especie ->
                            SpeciesStockSection(speciesStock = especie)
                        }
                    }
                } else if (!uiState.isLoading) {
                    EmptyStockState()
                }

                PullRefreshIndicator(
                    refreshing = uiState.isLoading,
                    state = pullRefreshState,
                    modifier = Modifier.align(Alignment.TopCenter),
                    backgroundColor = Color.White,
                    contentColor = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockFilterSection(
    unidades: List<UnidadProductiva>,
    selectedUnidadId: Int?,
    onSelectUnidad: (Int?) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(vertical = 16.dp)
    ) {
        Text(
            text = "Filtrar por Campo:",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF424242),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedUnidadId == null,
                    onClick = { onSelectUnidad(null) },
                    label = { Text("Todos") },
                    enabled = true,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SincPrimary.copy(alpha = 0.1f),
                        selectedLabelColor = SincPrimary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selectedUnidadId == null,
                        borderColor = if (selectedUnidadId == null) SincPrimary else Color.LightGray,
                        selectedBorderColor = SincPrimary
                    )
                )
            }
            items(unidades) { unidad ->
                FilterChip(
                    selected = selectedUnidadId == unidad.id,
                    onClick = { onSelectUnidad(unidad.id) },
                    label = { Text(unidad.nombre ?: "Campo ${unidad.id}") },
                    enabled = true,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SincPrimary.copy(alpha = 0.1f),
                        selectedLabelColor = SincPrimary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selectedUnidadId == unidad.id,
                        borderColor = if (selectedUnidadId == unidad.id) SincPrimary else Color.LightGray,
                        selectedBorderColor = SincPrimary
                    )
                )
            }
        }
    }
}

@Composable
private fun TotalStockSection(stock: ProcessedStock) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Stock Total",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color(0xFF757575)
                )
                Text(
                    text = stock.stockTotalGeneral.toString(),
                    style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF191C1E)
                )
                Text(
                    text = "Animales en campo",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF9E9E9E)
                )
            }
            if (stock.speciesDistribution.isNotEmpty()) {
                Box(modifier = Modifier.size(100.dp)) {
                    PieChart(
                        data = stock.speciesDistribution,
                        modifier = Modifier.fillMaxSize(),
                        strokeWidth = 35f
                    )
                }
            }
        }

        if (stock.speciesLegendItems.isNotEmpty()) {
            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = Color(0xFFF0F0F0))
            Spacer(modifier = Modifier.height(16.dp))
            
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                stock.speciesLegendItems.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(item.color)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = item.label,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF424242)
                            )
                        }
                        Text(
                            text = "${item.percentage.toInt()}%",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF191C1E)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SpeciesStockSection(
    speciesStock: ProcessedEspecieStock
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp)
            .animateContentSize(animationSpec = tween(300))
    ) {
        // Header de Especie
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(4.dp, 24.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(speciesStock.color)
            )
            Spacer(modifier = Modifier.width(12.dp))
            
            Text(
                text = speciesStock.nombre,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF191C1E),
                modifier = Modifier.weight(1f)
            )
            
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF5F7FA))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = speciesStock.stockTotal.toString(),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF191C1E)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Contenido (Filtros y Desglose)
        Column(modifier = Modifier.fillMaxWidth()) {
            var selectedGrouping by rememberSaveable { mutableStateOf(StockGrouping.BY_ALL) }

            val desgloseUiData = remember(selectedGrouping, speciesStock.desglose) {
                when (selectedGrouping) {
                    StockGrouping.BY_ALL -> DesgloseUiData.Full(speciesStock.desglose)
                    StockGrouping.BY_CATEGORY, StockGrouping.BY_BREED -> {
                        val groupedData = speciesStock.desglose
                            .groupBy { if (selectedGrouping == StockGrouping.BY_CATEGORY) it.categoria else it.raza }
                            .mapValues { entry -> entry.value.sumOf { it.quantity } }

                        val totalGroupValue = groupedData.values.sum().toFloat()
                        val legendItems = if (totalGroupValue > 0f) {
                            groupedData.entries.mapIndexed { index, entry ->
                                LegendItem(
                                    label = entry.key,
                                    value = entry.value,
                                    percentage = (entry.value / totalGroupValue) * 100,
                                    color = uiPieChartColors[index % uiPieChartColors.size]
                                )
                            }
                        } else {
                            emptyList()
                        }
                        val chartData = legendItems.map { PieChartData(value = it.value.toFloat(), color = it.color) }
                        DesgloseUiData.Grouped(chartData, legendItems)
                    }
                }
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                GroupingOptions(
                    selectedGrouping = selectedGrouping,
                    onGroupingSelected = { selectedGrouping = it },
                    selectedChipColor = speciesStock.color
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            DesgloseContent(desgloseUiData = desgloseUiData)
        }
    }
}


@Composable
private fun DesgloseContent(desgloseUiData: DesgloseUiData) {
    when (desgloseUiData) {
        is DesgloseUiData.Grouped -> {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (desgloseUiData.chartData.isNotEmpty()) {
                    // Tamaño ajustado y un poco más pequeño para evitar cortes
                    Box(modifier = Modifier.size(70.dp)) {
                        PieChart(
                            data = desgloseUiData.chartData,
                            modifier = Modifier.fillMaxSize(),
                            strokeWidth = 15f // Línea fina
                        )
                    }
                    Spacer(modifier = Modifier.width(24.dp))
                }
                
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    desgloseUiData.legendItems.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(item.color))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = item.label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF424242),
                                    maxLines = 1
                                )
                            }
                            Text(
                                text = "${item.percentage.toInt()}%",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF191C1E)
                            )
                        }
                    }
                }
            }
        }
        is DesgloseUiData.Full -> {
            Column(
                verticalArrangement = Arrangement.spacedBy(0.dp),
                modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(Color(0xFFF5F7FA))
            ) {
                // Cabecera de tabla
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Categoría", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF616161))
                    Text("Raza", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF616161))
                    Text("Cant.", modifier = Modifier.width(50.dp), fontWeight = FontWeight.Bold, textAlign = TextAlign.End, fontSize = 12.sp, color = Color(0xFF616161))
                }
                HorizontalDivider(color = Color(0xFFE0E0E0))
                
                // Filas
                desgloseUiData.tableData.forEachIndexed { index, item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(item.categoria, modifier = Modifier.weight(1f), fontSize = 13.sp, color = Color(0xFF424242))
                        Text(item.raza, modifier = Modifier.weight(1f), fontSize = 13.sp, color = Color(0xFF424242))
                        Text(item.quantity.toString(), modifier = Modifier.width(50.dp), textAlign = TextAlign.End, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF191C1E))
                    }
                    if (index < desgloseUiData.tableData.size - 1) {
                        HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 12.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyStockState() {
    Box(modifier = Modifier.fillMaxSize().padding(32.dp)) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = Color.Gray
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No se pudo cargar el stock.",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                color = Color.Gray
            )
            Text(
                text = "Desliza hacia abajo para reintentar.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = Color.LightGray
            )
        }
    }
}