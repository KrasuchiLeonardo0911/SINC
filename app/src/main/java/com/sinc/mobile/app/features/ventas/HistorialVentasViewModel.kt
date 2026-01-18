package com.sinc.mobile.app.features.ventas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sinc.mobile.domain.model.Catalogos
import com.sinc.mobile.domain.model.DeclaracionVenta
import com.sinc.mobile.domain.repository.CatalogosRepository
import com.sinc.mobile.domain.use_case.ventas.GetDeclaracionesVentaUseCase
import com.sinc.mobile.domain.use_case.ventas.SyncDeclaracionesVentaUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class HistorialVentasState(
    val isLoading: Boolean = false,
    val todasLasDeclaraciones: List<DeclaracionVenta> = emptyList(),
    val declaracionesFiltradas: List<DeclaracionVenta> = emptyList(),
    val filtroMes: LocalDate = LocalDate.now(), // Filtro por defecto: Mes actual
    val declaracionSeleccionada: DeclaracionVenta? = null,
    val catalogos: Catalogos? = null
)

@HiltViewModel
class HistorialVentasViewModel @Inject constructor(
    private val getDeclaracionesVentaUseCase: GetDeclaracionesVentaUseCase,
    private val syncDeclaracionesVentaUseCase: SyncDeclaracionesVentaUseCase,
    private val catalogosRepository: CatalogosRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistorialVentasState())
    val uiState: StateFlow<HistorialVentasState> = _uiState.asStateFlow()

    init {
        loadDeclaraciones()
        loadCatalogos()
    }

    private fun loadCatalogos() {
        viewModelScope.launch {
            catalogosRepository.getCatalogos().collect { catalogos ->
                _uiState.update { it.copy(catalogos = catalogos) }
            }
        }
    }

    private fun loadDeclaraciones() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            // Suscribirse al flujo de la base de datos
            getDeclaracionesVentaUseCase().collect { lista ->
                _uiState.update { state ->
                    val filtered = aplicarFiltro(lista, state.filtroMes)
                    state.copy(
                        isLoading = false,
                        todasLasDeclaraciones = lista,
                        declaracionesFiltradas = filtered
                    )
                }
            }
        }
    }

    fun onSyncRequested() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            syncDeclaracionesVentaUseCase()
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun cambiarMesFiltro(nuevoMes: LocalDate) {
        _uiState.update { state ->
            val filtered = aplicarFiltro(state.todasLasDeclaraciones, nuevoMes)
            state.copy(filtroMes = nuevoMes, declaracionesFiltradas = filtered)
        }
    }
    
    fun mesAnterior() {
        cambiarMesFiltro(_uiState.value.filtroMes.minusMonths(1))
    }
    
    fun mesSiguiente() {
        cambiarMesFiltro(_uiState.value.filtroMes.plusMonths(1))
    }

    fun seleccionarDeclaracion(declaracion: DeclaracionVenta?) {
        _uiState.update { it.copy(declaracionSeleccionada = declaracion) }
    }

    private fun aplicarFiltro(lista: List<DeclaracionVenta>, fechaFiltro: LocalDate): List<DeclaracionVenta> {
        return lista.filter { item ->
            try {
                // Asumiendo formato ISO 8601 o similar que empieza con YYYY-MM-DD
                val itemDate = LocalDate.parse(item.fechaDeclaracion.take(10)) 
                itemDate.month == fechaFiltro.month && itemDate.year == fechaFiltro.year
            } catch (e: Exception) {
                false
            }
        }
    }
}
