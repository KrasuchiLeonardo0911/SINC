package com.sinc.mobile.app.features.stock

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sinc.mobile.app.features.stock.components.LegendItem
import com.sinc.mobile.app.features.stock.components.PieChartData
import com.sinc.mobile.domain.model.Stock
import com.sinc.mobile.domain.model.UnidadProductiva
import com.sinc.mobile.domain.use_case.GetStockUseCase
import com.sinc.mobile.domain.use_case.GetUnidadesProductivasUseCase
import com.sinc.mobile.domain.use_case.SyncStockUseCase
import com.sinc.mobile.domain.use_case.SyncUnidadesProductivasUseCase
import com.sinc.mobile.domain.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// region Data Models
enum class StockGrouping {
    BY_ALL,
    BY_CATEGORY,
    BY_BREED
}

sealed class DesgloseItem { // Renamed for clarity
    data class Full(val categoria: String, val raza: String, val quantity: Int) : DesgloseItem()
}

sealed class DesgloseUiData {
    data class Grouped(val chartData: List<PieChartData>, val legendItems: List<LegendItem>) : DesgloseUiData()
    data class Full(val tableData: List<DesgloseItem.Full>) : DesgloseUiData()
}

data class ProcessedStock(
    val stockTotalGeneral: Int,
    val unidadesProductivas: List<ProcessedUnidadProductivaStock>,
    val allSpecies: List<ProcessedEspecieStock>,
    val speciesDistribution: List<PieChartData> = emptyList(),
    val speciesLegendItems: List<LegendItem> = emptyList() // New field
)

data class ProcessedUnidadProductivaStock(
    val nombre: String,
    val stockTotal: Int
)

data class ProcessedEspecieStock(
    val nombre: String,
    val stockTotal: Int,
    val desglose: List<DesgloseItem.Full>,
    val color: Color // NEW FIELD
)

data class StockUiState(

    val isLoading: Boolean = false,
    val unidadesProductivas: List<UnidadProductiva> = emptyList(),
    val error: String? = null,
    val stock: Stock? = null,
    val processedStock: ProcessedStock? = null
)
// endregion

@HiltViewModel

class StockViewModel @Inject constructor(
    private val getUnidadesProductivasUseCase: GetUnidadesProductivasUseCase,
    private val syncUnidadesProductivasUseCase: SyncUnidadesProductivasUseCase,
    private val getStockUseCase: GetStockUseCase,
    private val syncStockUseCase: SyncStockUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(StockUiState())
    val uiState: StateFlow<StockUiState> = _uiState.asStateFlow()

    private val pieChartColors = listOf(
        Color(0xFF6C5B7B), Color(0xFFC06C84), Color(0xFFF67280), Color(0xFFF8B195),
        Color(0xFFB39DDB), Color(0xFF81C784), Color(0xFFFFD54F), Color(0xFF4FC3F7),
        Color(0xFFE57373), Color(0xFF9575CD), Color(0xFF4DB6AC), Color(0xFFFFF176)
    )

    init {
        collectUnidadesProductivas()
        processAndCollectStock()
        refresh()
    }

    fun refresh() {

        viewModelScope.launch {

            if (_uiState.value.isLoading) return@launch
            _uiState.update { it.copy(isLoading = true, error = null) }
            val startTime = System.currentTimeMillis()
            val stockSyncResult = syncStockUseCase()
            syncUnidadesProductivasUseCase()

            if (stockSyncResult is Result.Failure) {
                _uiState.update { it.copy(error = stockSyncResult.error.message) }
            }

            val duration = System.currentTimeMillis() - startTime

            if (duration < 1000) {
                delay(1000 - duration)
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }
    private fun collectUnidadesProductivas() {
        getUnidadesProductivasUseCase().onEach { unidades ->
            _uiState.update { it.copy(unidadesProductivas = unidades) }
        }.launchIn(viewModelScope)
    }

    private fun processAndCollectStock() {
        viewModelScope.launch {
            getStockUseCase().collect { stock ->
                if (stock == null) {
                    _uiState.update { it.copy(stock = null, processedStock = null) }
                } else {
                    val processed = processStock(stock)
                    _uiState.update { it.copy(stock = stock, processedStock = processed) }
                }
            }
        }
    }

    internal fun processStock(stock: Stock): ProcessedStock {
        val speciesTotals = stock.unidadesProductivas
            .flatMap { it.especies }
            .groupBy { it.nombre }
            .mapValues { entry -> entry.value.sumOf { it.stockTotal } }
        val totalGeneralStock = speciesTotals.values.sum().toFloat()

        val speciesDistributionData = speciesTotals.entries.mapIndexed { index, entry ->
            PieChartData(
                value = entry.value.toFloat(),
                color = pieChartColors[index % pieChartColors.size]
            )
        }

        val speciesLegendItems = if (totalGeneralStock == 0f) {
            emptyList()
        } else {
            speciesTotals.entries.mapIndexed { index, entry ->
                LegendItem(
                    label = entry.key,
                    value = entry.value,
                    percentage = (entry.value / totalGeneralStock) * 100,
                    color = pieChartColors[index % pieChartColors.size]
                )
            }
        }

        val allSpeciesProcessed = stock.unidadesProductivas
            .flatMap { it.especies }
            .groupBy { it.nombre }
            .map { (nombreEspecie, especiesList) ->
                val desgloses = especiesList.flatMap { it.desglose }
                    .groupBy { Pair(it.categoria, it.raza) }
                    .map { (key, group) ->
                        DesgloseItem.Full(key.first, key.second, group.sumOf { it.cantidad })
                    }

                // Get the color for this species
                val speciesColor = speciesTotals.entries.firstOrNull { it.key == nombreEspecie }?.let { entry ->
                    pieChartColors[speciesTotals.keys.indexOf(entry.key) % pieChartColors.size]
                } ?: Color.Gray // Fallback color

                ProcessedEspecieStock(
                    nombre = nombreEspecie,
                    stockTotal = especiesList.sumOf { it.stockTotal },
                    desglose = desgloses,
                    color = speciesColor // Assign the color
                )
            }

        val unidadesProcessed = stock.unidadesProductivas.map {
            ProcessedUnidadProductivaStock(nombre = it.nombre, stockTotal = it.stockTotal)
        }

        return ProcessedStock(
            stockTotalGeneral = stock.stockTotalGeneral,
            unidadesProductivas = unidadesProcessed,
            allSpecies = allSpeciesProcessed,
            speciesDistribution = speciesDistributionData,
            speciesLegendItems = speciesLegendItems
        )
    }
}


