package com.sinc.mobile.app.features.stock

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sinc.mobile.domain.model.DesgloseStock
import com.sinc.mobile.domain.model.Stock
import com.sinc.mobile.domain.model.UnidadProductiva
import com.sinc.mobile.domain.use_case.GetStockUseCase
import com.sinc.mobile.domain.use_case.GetUnidadesProductivasUseCase
import com.sinc.mobile.domain.use_case.SyncStockUseCase
import com.sinc.mobile.domain.use_case.SyncUnidadesProductivasUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// region Data Models
enum class StockGrouping {
    BY_ALL,
    BY_CATEGORY,
    BY_BREED
}

sealed class DesgloseItem {
    data class Grouped(val name: String, val quantity: Int) : DesgloseItem()
    data class Full(val categoria: String, val raza: String, val quantity: Int) : DesgloseItem()
}

data class ProcessedStock(
    val stockTotalGeneral: Int,
    val unidadesProductivas: List<ProcessedUnidadProductivaStock>,
    val allSpecies: List<ProcessedEspecieStock>
)

data class ProcessedUnidadProductivaStock(
    val nombre: String,
    val stockTotal: Int
)

data class ProcessedEspecieStock(
    val nombre: String,
    val stockTotal: Int,
    val desglose: List<DesgloseItem>
)

data class StockUiState(
    val isLoading: Boolean = false,
    val unidadesProductivas: List<UnidadProductiva> = emptyList(),
    val error: String? = null,
    val stock: Stock? = null,
    val stockGrouping: StockGrouping = StockGrouping.BY_ALL, // Default to BY_ALL
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

    init {
        collectUnidadesProductivas()
        processAndCollectStock()
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            if (_uiState.value.isLoading) return@launch
            _uiState.update { it.copy(isLoading = true, error = null) }
            Log.d("StockViewModel", "[REFRESH] Iniciando. isLoading = true")

            val startTime = System.currentTimeMillis()
            val unidadesSyncJob = launch { syncUnidadesProductivasUseCase() }
            val stockSyncJob = launch {
                val result = syncStockUseCase()
                if (result is com.sinc.mobile.domain.util.Result.Failure) {
                    val errorMessage = result.error.message
                    _uiState.update { it.copy(error = errorMessage) }
                }
            }
            unidadesSyncJob.join()
            stockSyncJob.join()

            val duration = System.currentTimeMillis() - startTime
            if (System.currentTimeMillis() - startTime < 1000) {
                kotlinx.coroutines.delay(1000 - duration)
            }

            _uiState.update { it.copy(isLoading = false) }
            Log.d("StockViewModel", "[REFRESH] Finalizado. isLoading = false")
        }
    }

    fun setStockGrouping(grouping: StockGrouping) {
        _uiState.update { it.copy(stockGrouping = grouping) }
    }

    private fun collectUnidadesProductivas() {
        viewModelScope.launch {
            getUnidadesProductivasUseCase().collectLatest { unidades ->
                _uiState.update { it.copy(unidadesProductivas = unidades) }
            }
        }
    }

    private fun processAndCollectStock() {
        viewModelScope.launch {
            getStockUseCase().combine(uiState.map { it.stockGrouping }.distinctUntilChanged()) { stock, grouping ->
                Pair(stock, grouping)
            }.collect { (stock, grouping) ->
                if (stock == null) {
                    _uiState.update { it.copy(stock = null, processedStock = null) }
                    return@collect
                }

                val processed = processStock(stock, grouping)
                _uiState.update { it.copy(stock = stock, processedStock = processed) }
            }
        }
    }

    internal fun processStock(stock: Stock, grouping: StockGrouping): ProcessedStock {
        val allSpeciesProcessed = stock.unidadesProductivas
            .flatMap { it.especies }
            .groupBy { it.nombre }
            .map { (nombreEspecie, especiesList) ->
                val desgloses = especiesList.flatMap { it.desglose }

                val groupedDesglose: List<DesgloseItem> = when (grouping) {
                    StockGrouping.BY_ALL -> {
                        desgloses.groupBy { Pair(it.categoria, it.raza) }
                            .map { (key, group) ->
                                DesgloseItem.Full(key.first, key.second, group.sumOf { it.cantidad })
                            }
                    }
                    StockGrouping.BY_CATEGORY -> {
                        desgloses.groupBy { it.categoria }
                            .map { (name, group) ->
                                DesgloseItem.Grouped(name, group.sumOf { it.cantidad })
                            }
                    }
                    StockGrouping.BY_BREED -> {
                        desgloses.groupBy { it.raza }
                            .map { (name, group) ->
                                DesgloseItem.Grouped(name, group.sumOf { it.cantidad })
                            }
                    }
                }

                ProcessedEspecieStock(
                    nombre = nombreEspecie,
                    stockTotal = especiesList.sumOf { it.stockTotal },
                    desglose = groupedDesglose
                )
            }

        val unidadesProcessed = stock.unidadesProductivas.map {
            ProcessedUnidadProductivaStock(
                nombre = it.nombre,
                stockTotal = it.stockTotal
            )
        }

        return ProcessedStock(
            stockTotalGeneral = stock.stockTotalGeneral,
            unidadesProductivas = unidadesProcessed,
            allSpecies = allSpeciesProcessed
        )
    }
}
