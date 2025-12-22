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
        loadUnidadesProductivas()
        loadStock()
    }

    private fun loadStock() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val syncResult = syncStockUseCase() // Trigger sync from API
            if (syncResult is com.sinc.mobile.domain.util.Result.Failure) {
                Log.e("MainViewModel", "Error syncing stock: ${syncResult.error.message}")
                _uiState.update { it.copy(error = syncResult.error.message) }
            }

            getStockUseCase().collectLatest { stockData ->
                _uiState.update { it.copy(stock = stockData, isLoading = false) }
            }
        }
    }

    private fun loadUnidadesProductivas() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) } // Clear previous errors

            // Primero, intentar sincronizar las UPs desde la API
            val syncResult = syncUnidadesProductivasUseCase()

            // Luego, observar las UPs desde la base de datos local
            getUnidadesProductivasUseCase().collectLatest { unidades ->
                val errorMsg = if (syncResult is com.sinc.mobile.domain.util.Result.Failure) {
                    (syncResult.error as? com.sinc.mobile.domain.model.GenericError)?.message
                } else null

                _uiState.update { it.copy(
                    isLoading = false,
                    unidadesProductivas = unidades,
                    error = errorMsg,
                    shouldNavigateToCreateUnidadProductiva = false // Always navigate to home, user can go to create UP manually
                ) }
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