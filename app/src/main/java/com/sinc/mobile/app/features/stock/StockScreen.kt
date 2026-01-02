package com.sinc.mobile.app.features.stock

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.sinc.mobile.app.features.stock.components.GroupingOptions
import com.sinc.mobile.app.features.stock.components.LegendItem
import com.sinc.mobile.app.features.stock.components.PieChart
import com.sinc.mobile.app.features.stock.components.PieChartData
import com.sinc.mobile.app.features.stock.components.StockViewSelector
import com.sinc.mobile.app.ui.components.MinimalHeader
import com.sinc.mobile.ui.theme.SoftGray

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun StockScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: StockViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val processedStock = uiState.processedStock

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = SoftGray,
        topBar = {
            MinimalHeader(
                title = "Mi Stock",
                onBackPress = { navController.popBackStack() },
                modifier = Modifier.statusBarsPadding()
            )
        }
        // No BottomBar to provide more space
    ) { paddingValues ->
        val pullRefreshState = rememberPullRefreshState(refreshing = uiState.isLoading, onRefresh = viewModel::refresh)

        Box(
            Modifier
                .pullRefresh(pullRefreshState)
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            if (processedStock != null) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        TotalStockCard(processedStock)
                    }
                    items(processedStock.allSpecies) { especie ->
                        SpeciesStockCard(
                            speciesStock = especie
                        )
                    }
                }
            } else if (!uiState.isLoading) {
                // Empty state
                Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    Text(
                        text = "No se pudo cargar el stock. Desliza hacia abajo para reintentar.",
                        modifier = Modifier.align(Alignment.Center),
                        textAlign = TextAlign.Center
                    )
                }
            }

            PullRefreshIndicator(
                refreshing = uiState.isLoading,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

@Composable
private fun TotalStockCard(stock: ProcessedStock) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Stock Total General", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = stock.stockTotalGeneral.toString(),
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
                if (stock.speciesDistribution.isNotEmpty()) {
                    PieChart(
                        data = stock.speciesDistribution,
                        modifier = Modifier.size(80.dp),
                        strokeWidth = 15f
                    )
                }
            }

            // Legend for species distribution
            if (stock.speciesLegendItems.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    stock.speciesLegendItems.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(item.color))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = item.label, style = MaterialTheme.typography.bodySmall)
                            }
                            Text(text = "%.0f".format(item.percentage) + "%", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// Define colors at the top level of the file for reuse
private val pieChartColors = listOf(
    Color(0xFF6C5B7B), Color(0xFFC06C84), Color(0xFFF67280), Color(0xFFF8B195),
    Color(0xFFB39DDB), Color(0xFF81C784), Color(0xFFFFD54F), Color(0xFF4FC3F7),
    Color(0xFFE57373), Color(0xFF9575CD), Color(0xFF4DB6AC), Color(0xFFFFF176)
)

@Composable
private fun SpeciesStockCard(
    speciesStock: ProcessedEspecieStock
) {
    var isExpanded by rememberSaveable { mutableStateOf(false) } // Collapsed by default

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded }, // Clickable on the Card
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .animateContentSize(animationSpec = tween(250)) // Faster animation
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth() // No longer clickable
            ) {
                Text(
                    text = speciesStock.nombre,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = speciesStock.stockTotal.toString(),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Colapsar" else "Expandir",
                    modifier = Modifier.rotate(if (isExpanded) 180f else 0f)
                )
            }

            // Collapsible content
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn(animationSpec = tween(200, delayMillis = 50)),
                exit = fadeOut(animationSpec = tween(100))
            ) {
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
                                            color = pieChartColors[index % pieChartColors.size]
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

                    Spacer(modifier = Modifier.height(8.dp))
                    // Filters
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Filtros:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Light
                        )
                        GroupingOptions(
                            selectedGrouping = selectedGrouping,
                            onGroupingSelected = { selectedGrouping = it },
                            selectedChipColor = speciesStock.color // Pass the color here
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(16.dp))
                    // Content
                    DesgloseContent(desgloseUiData = desgloseUiData)
                }
            }
        }
    }
}


@Composable
private fun DesgloseContent(desgloseUiData: DesgloseUiData) {
    when (desgloseUiData) {
        is DesgloseUiData.Grouped -> {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (desgloseUiData.chartData.isNotEmpty()) {
                    PieChart(
                        data = desgloseUiData.chartData,
                        modifier = Modifier.size(120.dp),
                        strokeWidth = 20f
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                // Legend
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    desgloseUiData.legendItems.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(item.color))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = item.label, style = MaterialTheme.typography.bodyMedium)
                            }
                            Text(text = "%.0f".format(item.percentage) + "%", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
        is DesgloseUiData.Full -> {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Categoría", modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium, fontSize = 13.sp)
                    Text("Raza", modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium, fontSize = 13.sp)
                    Text("Cantidad", modifier = Modifier.width(72.dp), fontWeight = FontWeight.Medium, textAlign = TextAlign.End, fontSize = 13.sp)
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                // Rows
                desgloseUiData.tableData.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(item.categoria, modifier = Modifier.weight(1f), fontSize = 14.sp)
                        Text(item.raza, modifier = Modifier.weight(1f), fontSize = 14.sp)
                        Text(item.quantity.toString(), modifier = Modifier.width(72.dp), textAlign = TextAlign.End, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}