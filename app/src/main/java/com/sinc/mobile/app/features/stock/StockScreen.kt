package com.sinc.mobile.app.features.stock

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.sinc.mobile.app.features.stock.components.GroupingOptions
import com.sinc.mobile.app.features.stock.components.PieChart
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
                onBackPress = { navController.popBackStack() }
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
                            speciesStock = especie,
                            grouping = uiState.stockGrouping,
                            onGroupingSelected = viewModel::setStockGrouping
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

@Composable
private fun SpeciesStockCard(
    speciesStock: ProcessedEspecieStock,
    grouping: StockGrouping,
    onGroupingSelected: (StockGrouping) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = speciesStock.nombre,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = speciesStock.stockTotal.toString(),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
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
                    selectedGrouping = grouping,
                    onGroupingSelected = onGroupingSelected
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))
            // Content
            DesgloseContent(desgloseUiData = speciesStock.desglose)
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