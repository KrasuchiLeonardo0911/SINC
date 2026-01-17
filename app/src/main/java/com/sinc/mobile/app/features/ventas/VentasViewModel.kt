package com.sinc.mobile.app.features.ventas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sinc.mobile.domain.model.*
import com.sinc.mobile.domain.repository.CatalogosRepository
import com.sinc.mobile.domain.use_case.GetUnidadesProductivasUseCase
import com.sinc.mobile.domain.use_case.SyncCatalogosUseCase
import com.sinc.mobile.domain.use_case.SyncStockUseCase
import com.sinc.mobile.domain.use_case.SyncUnidadesProductivasUseCase
import com.sinc.mobile.domain.use_case.ventas.CreateDeclaracionVentaUseCase
import com.sinc.mobile.domain.use_case.ventas.GetDeclaracionesVentaUseCase
import com.sinc.mobile.domain.use_case.ventas.SyncDeclaracionesVentaUseCase
import com.sinc.mobile.domain.use_case.ventas.ValidateStockForVentaUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.sinc.mobile.domain.util.Result

data class VentasState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    
    // Listas de datos
    val unidadesProductivas: List<UnidadProductiva> = emptyList(),
    val especies: List<Especie> = emptyList(),
    val razas: List<Raza> = emptyList(),
    val categorias: List<Categoria> = emptyList(),
    val declaracionesPendientes: List<DeclaracionVenta> = emptyList(),

    // Formulario
    val selectedUpId: Int? = null,
    val selectedEspecieId: Int? = null,
    val selectedRazaId: Int? = null,
    val selectedCategoriaId: Int? = null,
    val cantidad: String = "",
    val observaciones: String = "",
    
    // Estado de Validación
    val stockValidationMessage: String? = null
)

@HiltViewModel
class VentasViewModel @Inject constructor(
    private val getDeclaracionesVentaUseCase: GetDeclaracionesVentaUseCase,
    private val createDeclaracionVentaUseCase: CreateDeclaracionVentaUseCase,
    private val syncDeclaracionesVentaUseCase: SyncDeclaracionesVentaUseCase,
    private val validateStockForVentaUseCase: ValidateStockForVentaUseCase,
    private val getUnidadesProductivasUseCase: GetUnidadesProductivasUseCase,
    private val syncUnidadesProductivasUseCase: SyncUnidadesProductivasUseCase,
    private val syncCatalogosUseCase: SyncCatalogosUseCase,
    private val syncStockUseCase: SyncStockUseCase,
    private val catalogosRepository: CatalogosRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(VentasState())
    val uiState: StateFlow<VentasState> = _uiState.asStateFlow()

    init {
        loadInitialData()
        syncData()
        observeDeclaraciones()
    }

    private fun syncData() {
        viewModelScope.launch {
            // Trigger sync in background to ensure data availability
            syncUnidadesProductivasUseCase()
            syncCatalogosUseCase()
            syncStockUseCase()
        }
    }

    private fun loadInitialData() {
        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            combine(
                getUnidadesProductivasUseCase(),
                catalogosRepository.getMovimientoCatalogos() // Use lighter method
            ) { ups, catalogos ->
                Pair(ups, catalogos)
            }.collect { (ups, catalogos) ->
                _uiState.update { state ->
                    val newState = state.copy(
                        isLoading = false,
                        unidadesProductivas = ups,
                        especies = catalogos.especies
                    )
                    
                    // Si ya hay selecciones, filtrar razas/categorías
                    val newRazas = if (newState.selectedEspecieId != null) 
                        catalogos.razas.filter { it.especieId == newState.selectedEspecieId } 
                    else newState.razas
                    
                    val newCategorias = if (newState.selectedEspecieId != null)
                        catalogos.categorias.filter { it.especieId == newState.selectedEspecieId }
                    else newState.categorias
                    
                    // Pre-seleccionar UP si solo hay una y no se ha seleccionado nada aún
                    val finalSelectedUpId = if (newState.selectedUpId == null && ups.size == 1) ups.first().id else newState.selectedUpId

                    newState.copy(
                        razas = newRazas,
                        categorias = newCategorias,
                        selectedUpId = finalSelectedUpId
                    )
                }
            }
        }
    }

    private fun observeDeclaraciones() {
        viewModelScope.launch {
            getDeclaracionesVentaUseCase().collect { declaraciones ->
                _uiState.update { it.copy(declaracionesPendientes = declaraciones) }
            }
        }
    }

    fun onUpSelected(upId: Int) {
        _uiState.update { it.copy(selectedUpId = upId) }
    }

    fun onEspecieSelected(especieId: Int) {
        viewModelScope.launch {
            val catalogos = catalogosRepository.getMovimientoCatalogos().first()
            _uiState.update { state ->
                state.copy(
                    selectedEspecieId = especieId,
                    selectedRazaId = null,
                    selectedCategoriaId = null,
                    razas = catalogos.razas.filter { it.especieId == especieId },
                    categorias = catalogos.categorias.filter { it.especieId == especieId }
                )
            }
        }
    }

    fun onRazaSelected(razaId: Int) {
        _uiState.update { it.copy(selectedRazaId = razaId) }
    }

    fun onCategoriaSelected(categoriaId: Int) {
        _uiState.update { it.copy(selectedCategoriaId = categoriaId) }
    }

    fun onCantidadChanged(cantidad: String) {
        // Solo permitir números
        if (cantidad.all { it.isDigit() }) {
            _uiState.update { it.copy(cantidad = cantidad, stockValidationMessage = null) }
        }
    }

    fun onObservacionesChanged(observaciones: String) {
        _uiState.update { it.copy(observaciones = observaciones) }
    }

    fun onSubmit() {
        viewModelScope.launch {
            val state = _uiState.value
            val cantidadInt = state.cantidad.toIntOrNull()

            if (state.selectedUpId == null || state.selectedEspecieId == null || 
                state.selectedRazaId == null || state.selectedCategoriaId == null || 
                cantidadInt == null || cantidadInt <= 0) {
                _uiState.update { it.copy(error = "Por favor complete todos los campos requeridos correctamente.") }
                return@launch
            }

            _uiState.update { it.copy(isLoading = true, error = null, stockValidationMessage = null) }

            // 1. Validar Stock
            val validationResult = validateStockForVentaUseCase(
                unidadProductivaId = state.selectedUpId,
                especieId = state.selectedEspecieId,
                razaId = state.selectedRazaId,
                categoriaAnimalId = state.selectedCategoriaId,
                cantidadSolicitada = cantidadInt
            )

            when (validationResult) {
                is ValidateStockForVentaUseCase.ValidationResult.Success -> {
                    // 2. Crear Declaración
                    val result = createDeclaracionVentaUseCase(
                        unidadProductivaId = state.selectedUpId,
                        especieId = state.selectedEspecieId,
                        razaId = state.selectedRazaId,
                        categoriaAnimalId = state.selectedCategoriaId,
                        cantidad = cantidadInt,
                        observaciones = state.observaciones
                    )

                    when (result) {
                        is Result.Success -> {
                            _uiState.update { 
                                it.copy(
                                    isLoading = false, 
                                    successMessage = "Venta registrada correctamente.",
                                    // Limpiar formulario parcial
                                    cantidad = "",
                                    observaciones = ""
                                )
                            }
                        }
                        is Result.Failure -> {
                            val msg = if (result.error is GenericError) (result.error as GenericError).message else "Error al guardar"
                            _uiState.update { it.copy(isLoading = false, error = msg) }
                        }
                    }
                }
                is ValidateStockForVentaUseCase.ValidationResult.InsufficientStock -> {
                    val msg = "Stock insuficiente. Disponible: ${validationResult.real - validationResult.pendiente} (Real: ${validationResult.real}, Pendientes: ${validationResult.pendiente})"
                    _uiState.update { it.copy(isLoading = false, error = msg) }
                }
                is ValidateStockForVentaUseCase.ValidationResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = validationResult.message) }
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }
    
    fun onSyncRequested() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            syncDeclaracionesVentaUseCase()
            _uiState.update { it.copy(isLoading = false) }
        }
    }
}
