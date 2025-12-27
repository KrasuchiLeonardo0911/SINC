package com.sinc.mobile.app.features.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sinc.mobile.domain.model.UnidadProductiva
import com.sinc.mobile.domain.model.Stock
import com.sinc.mobile.domain.use_case.GetUnidadesProductivasUseCase
import com.sinc.mobile.domain.use_case.SyncUnidadesProductivasUseCase
import com.sinc.mobile.domain.use_case.GetStockUseCase
import com.sinc.mobile.domain.use_case.SyncStockUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getUnidadesProductivasUseCase: GetUnidadesProductivasUseCase,
    private val syncUnidadesProductivasUseCase: SyncUnidadesProductivasUseCase,
    private val getStockUseCase: GetStockUseCase,
    private val syncStockUseCase: SyncStockUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        // Start collecting data from the local database immediately
        collectUnidadesProductivas()
        collectStock()

        // Perform initial sync from the network
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            if (_uiState.value.isLoading) return@launch
            _uiState.update { it.copy(isLoading = true, error = null) }
            Log.d("MainViewModel", "[REFRESH] Iniciando. isLoading = true")

            val startTime = System.currentTimeMillis()

            // Launch sync tasks in parallel
            val unidadesSyncJob = launch {
                Log.d("MainViewModel", "[REFRESH] Sincronizando unidades productivas...")
                val result = syncUnidadesProductivasUseCase()
                if (result is com.sinc.mobile.domain.util.Result.Failure) {
                    val errorMessage = when (result.error) {
                        is com.sinc.mobile.domain.model.GenericError -> (result.error as com.sinc.mobile.domain.model.GenericError).message
                        else -> "Error desconocido al sincronizar unidades productivas"
                    }
                    Log.e("MainViewModel", "[REFRESH] Error en unidades: $errorMessage")
                } else {
                    Log.d("MainViewModel", "[REFRESH] Unidades sincronizadas.")
                }
            }
            val stockSyncJob = launch {
                Log.d("MainViewModel", "[REFRESH] Sincronizando stock...")
                val result = syncStockUseCase()
                if (result is com.sinc.mobile.domain.util.Result.Failure) {
                    val errorMessage = when (result.error) {
                        is com.sinc.mobile.domain.model.GenericError -> (result.error as com.sinc.mobile.domain.model.GenericError).message
                        else -> "Error desconocido al sincronizar stock"
                    }
                    Log.e("MainViewModel", "[REFRESH] Error en stock: $errorMessage")
                    _uiState.update { it.copy(error = errorMessage) }
                } else {
                    Log.d("MainViewModel", "[REFRESH] Stock sincronizado.")
                }
            }

            Log.d("MainViewModel", "[REFRESH] Esperando que finalicen los jobs...")
            unidadesSyncJob.join()
            stockSyncJob.join()
            Log.d("MainViewModel", "[REFRESH] Jobs finalizados.")

            val duration = System.currentTimeMillis() - startTime
            val remainingDelay = 1000L - duration
            if (remainingDelay > 0) {
                kotlinx.coroutines.delay(remainingDelay)
            }

            _uiState.update { it.copy(isLoading = false) }
            Log.d("MainViewModel", "[REFRESH] Finalizado. isLoading = false")
        }
    }

    private fun collectStock() {
        viewModelScope.launch {
            getStockUseCase().collectLatest { stockData ->
                Log.d("MainViewModel", "Nuevo stock recibido del DB Flow: $stockData")
                _uiState.update { it.copy(stock = stockData) }
            }
        }
    }

    private fun collectUnidadesProductivas() {
        viewModelScope.launch {
            getUnidadesProductivasUseCase().collectLatest { unidades ->
                Log.d("MainViewModel", "Nuevas unidades productivas recibidas del DB Flow: $unidades")
                _uiState.update { it.copy(unidadesProductivas = unidades) }
            }
        }
    }

    fun resetNavigationToCreateUnidadProductiva() {
        _uiState.update { it.copy(shouldNavigateToCreateUnidadProductiva = false) }
    }
}

data class MainUiState(
    val isLoading: Boolean = false,
    val unidadesProductivas: List<UnidadProductiva> = emptyList(),
    val error: String? = null,
    val shouldNavigateToCreateUnidadProductiva: Boolean = false,
    val stock: Stock? = null // New field for stock data
)