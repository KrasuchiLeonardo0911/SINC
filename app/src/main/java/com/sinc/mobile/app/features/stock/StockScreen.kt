package com.sinc.mobile.app.features.stock

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.sinc.mobile.app.features.stock.components.GroupingOptions
import com.sinc.mobile.app.features.stock.components.StockAccordion
import com.sinc.mobile.app.features.stock.components.StockViewSelector
import com.sinc.mobile.app.ui.components.MinimalHeader
import com.sinc.mobile.domain.model.Stock
import com.sinc.mobile.domain.model.UnidadProductiva
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun StockScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: StockViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val processedStock = uiState.processedStock

    Log.d("StockScreen", "StockScreen recompose. Loading: ${uiState.isLoading}")
    var selectedView by remember { mutableStateOf<Any>("Total") }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            MinimalHeader(
                title = "Mi Stock",
                onBackPress = { navController.popBackStack() }
            )
        }
    ) { paddingValues ->
        val pullRefreshState = rememberPullRefreshState(refreshing = uiState.isLoading, onRefresh = viewModel::refresh)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            StockViewSelector(
                unidades = uiState.unidadesProductivas,
                selectedView = selectedView,
                onSelectionChanged = { selectedView = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            GroupingOptions(
                selectedGrouping = uiState.stockGrouping,
                onGroupingSelected = viewModel::setStockGrouping
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(Modifier.pullRefresh(pullRefreshState)) {
                if (processedStock != null) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        when (val selection = selectedView) {
                            is String -> { // "Total" view
                                if (processedStock.stockTotalGeneral == 0) {
                                    item { Text("No hay stock registrado en el sistema.", modifier = Modifier.padding(top = 16.dp)) }
                                } else {
                                    item {
                                        StockAccordion(
                                            header = {
                                                Text("Stock Total General: ${processedStock.stockTotalGeneral}", fontWeight = FontWeight.Bold)
                                            },
                                            content = {
                                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                    processedStock.unidadesProductivas.forEach { up ->
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.SpaceBetween
                                                        ) {
                                                            Text(up.nombre, fontWeight = FontWeight.SemiBold)
                                                            Text(up.stockTotal.toString())
                                                        }
                                                    }
                                                }
                                            },
                                            initiallyExpanded = true
                                        )
                                    }
                                    items(processedStock.allSpecies) { especie ->
                                        StockAccordion(
                                            header = { Text("${especie.nombre}: ${especie.stockTotal}", fontWeight = FontWeight.SemiBold) },
                                            content = {
                                                DesgloseContent(
                                                    desglose = especie.desglose,
                                                    grouping = uiState.stockGrouping
                                                )
                                            }
                                        )
                                    }
                                }
                            }
                            is UnidadProductiva -> {
                                val upStock = uiState.stock?.unidadesProductivas?.find { it.id == selection.id }
                                if (upStock != null && upStock.stockTotal > 0) {
                                    item {
                                        StockAccordion(
                                            header = {
                                                Text("Stock Total: ${upStock.stockTotal}", fontWeight = FontWeight.Bold)
                                            },
                                            content = {},
                                            initiallyExpanded = true
                                        )
                                    }
                                    items(upStock.especies) { especie ->
                                        val processedEspecie = viewModel.processStock(
                                            Stock(
                                                stockTotalGeneral = upStock.stockTotal,
                                                unidadesProductivas = listOf(upStock)
                                            ),
                                            uiState.stockGrouping
                                        ).allSpecies.find { it.nombre == especie.nombre }

                                        if(processedEspecie != null) {
                                            StockAccordion(
                                                header = { Text("${especie.nombre}: ${especie.stockTotal}", fontWeight = FontWeight.SemiBold) },
                                                content = {
                                                    DesgloseContent(
                                                        desglose = processedEspecie.desglose,
                                                        grouping = uiState.stockGrouping
                                                    )
                                                }
                                            )
                                        }
                                    }
                                } else {
                                    item { Text("No hay stock registrado en este campo.", modifier = Modifier.padding(top = 16.dp)) }
                                }
                            }
                        }
                    }
                } else if (!uiState.isLoading) {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        item {
                            Box(modifier = Modifier.fillParentMaxSize().padding(16.dp)) {
                                Text(
                                    text = "No se pudo cargar el stock. Desliza hacia abajo para reintentar.",
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        }
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
}

@Composable
fun DesgloseContent(desglose: List<DesgloseItem>, grouping: StockGrouping) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            when (grouping) {
                StockGrouping.BY_ALL -> {
                    Text("Categoría", modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium, fontSize = 13.sp)
                    Text("Raza", modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium, fontSize = 13.sp)
                }
                StockGrouping.BY_CATEGORY -> {
                    Text("Categoría", modifier = Modifier.weight(2f), fontWeight = FontWeight.Medium, fontSize = 13.sp)
                }
                StockGrouping.BY_BREED -> {
                    Text("Raza", modifier = Modifier.weight(2f), fontWeight = FontWeight.Medium, fontSize = 13.sp)
                }
            }
            Text("Cantidad", modifier = Modifier.width(72.dp), fontWeight = FontWeight.Medium, textAlign = TextAlign.End, fontSize = 13.sp)
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        // Rows
        desglose.forEach { item ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                when (item) {
                    is DesgloseItem.Full -> {
                        Text(item.categoria, modifier = Modifier.weight(1f))
                        Text(item.raza, modifier = Modifier.weight(1f))
                        Text(item.quantity.toString(), modifier = Modifier.width(72.dp), textAlign = TextAlign.End)
                    }
                    is DesgloseItem.Grouped -> {
                        Text(item.name, modifier = Modifier.weight(2f))
                        Text(item.quantity.toString(), modifier = Modifier.width(72.dp), textAlign = TextAlign.End)
                    }
                }
            }
        }
    }
}