package com.sinc.mobile.app.features.historial_movimientos

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sinc.mobile.domain.model.MovimientoHistorial
import com.sinc.mobile.domain.use_case.GetMovimientosHistorialUseCase
import com.sinc.mobile.domain.use_case.SyncMovimientosHistorialUseCase
import com.sinc.mobile.domain.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

data class HistorialMovimientosState(
    val isInitialLoad: Boolean = true,
    val allMovimientos: List<MovimientoHistorial> = emptyList(),
    val filteredMovimientos: List<MovimientoHistorial> = emptyList(),
    val selectedDate: LocalDate = LocalDate.now(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class HistorialMovimientosViewModel @Inject constructor(
    private val getMovimientosHistorialUseCase: GetMovimientosHistorialUseCase,
    private val syncMovimientosHistorialUseCase: SyncMovimientosHistorialUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(HistorialMovimientosState())
    val state = _state.asStateFlow()

    init {
        loadMovimientos()
        initialSync()
    }

    private fun initialSync() {
        viewModelScope.launch {
            _state.update { it.copy(isInitialLoad = true, error = null) }
            val startTime = System.currentTimeMillis()
            
            // Sincronización en segundo plano bajo la pantalla blanca
            syncMovimientosHistorialUseCase()
            
            val duration = System.currentTimeMillis() - startTime
            if (duration < 1500) {
                delay(1500 - duration)
            }
            _state.update { it.copy(isInitialLoad = false) }
        }
    }

    private fun loadMovimientos() {
        getMovimientosHistorialUseCase()
            .onEach { movimientos ->
                _state.update { 
                    it.copy(allMovimientos = movimientos) 
                }
                filterMovimientos()
            }
            .launchIn(viewModelScope)
    }

    private fun filterMovimientos() {
        val currentState = _state.value
        val selectedMonth = YearMonth.from(currentState.selectedDate)
        
        val filtered = currentState.allMovimientos.filter {
            YearMonth.from(it.fechaRegistro) == selectedMonth
        }
        
        _state.update { it.copy(filteredMovimientos = filtered) }
    }

    fun previousMonth() {
        _state.update { it.copy(selectedDate = it.selectedDate.minusMonths(1)) }
        filterMovimientos()
    }

    fun nextMonth() {
        _state.update { it.copy(selectedDate = it.selectedDate.plusMonths(1)) }
        filterMovimientos()
    }

    fun syncMovimientos() {
        viewModelScope.launch {
            if (_state.value.isLoading) return@launch
            _state.update { it.copy(isLoading = true, error = null) }
            
            val result = syncMovimientosHistorialUseCase()

            if (result is Result.Failure) {
                _state.update { it.copy(error = result.error.message) }
            }

            _state.update { it.copy(isLoading = false) }
        }
    }
}