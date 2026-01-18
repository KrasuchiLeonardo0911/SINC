package com.sinc.mobile.app.features.historial_movimientos.resumen

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sinc.mobile.domain.model.MovimientoHistorial
import com.sinc.mobile.domain.use_case.GetMovimientosHistorialUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.Month
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject

data class SpeciesSummary(
    val speciesName: String,
    val totalAltas: Int,
    val totalBajas: Int
)

data class ResumenMovimientosState(
    val month: Int = 1,
    val year: Int = 2026,
    val monthName: String = "",
    val totalAltas: Int = 0,
    val totalBajas: Int = 0,
    val balance: Int = 0,
    val speciesSummary: List<SpeciesSummary> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class ResumenMovimientosViewModel @Inject constructor(
    private val getMovimientosHistorialUseCase: GetMovimientosHistorialUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(ResumenMovimientosState())
    val state = _state.asStateFlow()

    init {
        val month = savedStateHandle.get<Int>("month") ?: LocalDate.now().monthValue
        val year = savedStateHandle.get<Int>("year") ?: LocalDate.now().year

        val monthName = Month.of(month).getDisplayName(TextStyle.FULL, Locale("es", "ES"))
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

        _state.update {
            it.copy(
                month = month,
                year = year,
                monthName = monthName
            )
        }

        loadData(month, year)
    }

    private fun loadData(month: Int, year: Int) {
        getMovimientosHistorialUseCase().onEach { allMovimientos ->
            val targetMonth = YearMonth.of(year, month)
            
            val filteredMovimientos = allMovimientos.filter {
                YearMonth.from(it.fechaRegistro) == targetMonth
            }

            calculateSummary(filteredMovimientos)
        }.launchIn(viewModelScope)
    }

    private fun calculateSummary(movimientos: List<MovimientoHistorial>) {
        var totalAltas = 0
        var totalBajas = 0
        val speciesMap = mutableMapOf<String, Pair<Int, Int>>() // Name -> (Altas, Bajas)

        movimientos.forEach { mov ->
            val isAlta = mov.tipoMovimiento.equals("alta", ignoreCase = true)
            val qty = mov.cantidad

            if (isAlta) {
                totalAltas += qty
            } else {
                totalBajas += qty
            }

            val current = speciesMap.getOrDefault(mov.especie, 0 to 0)
            if (isAlta) {
                speciesMap[mov.especie] = current.first + qty to current.second
            } else {
                speciesMap[mov.especie] = current.first to current.second + qty
            }
        }

        val summaryList = speciesMap.map { (name, counts) ->
            SpeciesSummary(name, counts.first, counts.second)
        }.sortedBy { it.speciesName }

        _state.update {
            it.copy(
                totalAltas = totalAltas,
                totalBajas = totalBajas,
                balance = totalAltas - totalBajas,
                speciesSummary = summaryList,
                isLoading = false
            )
        }
    }
}
