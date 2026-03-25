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
import com.sinc.mobile.domain.use_case.ventas.CancelDeclaracionVentaUseCase
import com.sinc.mobile.domain.model.venta.LogisticaStatus
import com.sinc.mobile.domain.use_case.GetStockUseCase
import com.sinc.mobile.domain.util.NetworkMonitor
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
    val declaracionesActivas: List<DeclaracionVenta> = emptyList(),

    // Formulario
    val selectedUpId: Int? = null,
    val selectedEspecieId: Int? = null,
    val selectedRazaId: Int? = null,
    val selectedCategoriaId: Int? = null,
    val cantidad: String = "1",
    val observaciones: String = "",
    val pesoAproximado: String = "",
    
    // Estado de Validación
    val stockValidationMessage: String? = null,
    
    // Logística
    val isLogisticsOpen: Boolean = true,
    val logisticsMessage: String? = null,
    val nextVisitDate: String? = null,
    val orderDeadline: String? = null,
    val isOnline: Boolean = true
)

@HiltViewModel
class VentasViewModel @Inject constructor(
    private val getDeclaracionesVentaUseCase: GetDeclaracionesVentaUseCase,
    private val createDeclaracionVentaUseCase: CreateDeclaracionVentaUseCase,
    private val syncDeclaracionesVentaUseCase: SyncDeclaracionesVentaUseCase,
    private val cancelDeclaracionVentaUseCase: CancelDeclaracionVentaUseCase,
    private val validateStockForVentaUseCase: ValidateStockForVentaUseCase,
    private val getUnidadesProductivasUseCase: GetUnidadesProductivasUseCase,
    private val syncUnidadesProductivasUseCase: SyncUnidadesProductivasUseCase,
    private val syncCatalogosUseCase: SyncCatalogosUseCase,
    private val syncStockUseCase: SyncStockUseCase,
    private val catalogosRepository: CatalogosRepository,
    private val getLogisticsStatusUseCase: com.sinc.mobile.domain.use_case.ventas.GetLogisticsStatusUseCase,
    private val sessionManager: com.sinc.mobile.data.session.SessionManager,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    private val _uiState = MutableStateFlow(VentasState())
    val uiState: StateFlow<VentasState> = _uiState.asStateFlow()

    init {
        checkLogisticsStatus()
        observeNetwork()
        observeDeclaraciones()
        
        viewModelScope.launch {
            if (networkMonitor.isOnline.first()) {
                loadInitialData()
                syncData()
            }
        }
    }

    private fun observeNetwork() {
        viewModelScope.launch {
            networkMonitor.isOnline.collect { isOnline ->
                _uiState.update { it.copy(isOnline = isOnline) }
                // If we come back online, trigger a sync
                if (isOnline) {
                    syncData()
                }
            }
        }
    }

    private fun checkLogisticsStatus() {
        val isOpen = getLogisticsStatusUseCase()
        _uiState.update { 
            it.copy(
                isLogisticsOpen = isOpen,
                logisticsMessage = if (!isOpen) "El periodo de inscripciones ha cerrado." else null,
                nextVisitDate = sessionManager.getNextVisitDate(),
                orderDeadline = sessionManager.getOrderDeadline()
            )
        }
    }

    private fun syncData() {
        viewModelScope.launch {
            syncUnidadesProductivasUseCase()
            syncCatalogosUseCase()
            syncStockUseCase()
            syncDeclaracionesVentaUseCase()
        }
    }

    private fun loadInitialData() {
        _uiState.update { it.copy(isLoading = true) }

        // Cargar UPs
        viewModelScope.launch {
            getUnidadesProductivasUseCase().collect { ups ->
                _uiState.update { state ->
                    val newState = state.copy(
                        isLoading = false, // Asumimos carga inicial lista cuando llegan UPs
                        unidadesProductivas = ups
                    )
                    
                    // Pre-seleccionar UP si solo hay una
                    if (newState.selectedUpId == null && ups.size == 1) {
                        onUpSelected(ups.first().id)
                        return@update newState.copy(selectedUpId = ups.first().id)
                    }
                    newState
                }
            }
        }

        // Cargar Catálogos
        viewModelScope.launch {
            catalogosRepository.getMovimientoCatalogos().collect { catalogos ->
                _uiState.update { state ->
                    if (state.selectedEspecieId != null) {
                        state.copy(
                            especies = catalogos.especies,
                            razas = catalogos.razas.filter { it.especieId == state.selectedEspecieId },
                            categorias = catalogos.categorias.filter { it.especieId == state.selectedEspecieId }
                        )
                    } else if (state.selectedUpId == null) {
                        state.copy(especies = catalogos.especies)
                    } else {
                        state
                    }
                }
            }
        }
    }

    private fun observeDeclaraciones() {
        viewModelScope.launch {
            getDeclaracionesVentaUseCase()
                .map { todas ->
                    if (todas.isEmpty()) return@map emptyList<DeclaracionVenta>()

                    // 1. Encontrar el ID de ciclo más reciente
                    val maxCicloId = todas.maxOf { it.historialCicloId ?: 0 }
                    
                    // 2. Filtrar solo las de ese ciclo
                    val loteActual = todas.filter { it.historialCicloId == maxCicloId }

                    // 3. Verificar si el lote completo ha finalizado
                    // Un lote finaliza si todos sus items están en estados terminales
                    val todoFinalizado = loteActual.all { 
                        it.estado == LogisticaStatus.ENTREGADO.key || 
                        it.estado == LogisticaStatus.RECHAZADO_FINAL.key ||
                        it.estado == LogisticaStatus.CANCELADO_REGRESANDO.key ||
                        it.estado.contains("rechazado")
                    }

                    // Si todo terminó, devolvemos lista vacía para "Seguimiento" (limpia la pantalla)
                    if (todoFinalizado) emptyList() else loteActual
                }
                .collect { filteredDeclaraciones ->
                    _uiState.update { it.copy(declaracionesActivas = filteredDeclaraciones) }
                }
        }
    }

    fun onCancelDeclaracion(id: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = cancelDeclaracionVentaUseCase(id)
            when (result) {
                is Result.Success -> {
                    _uiState.update { it.copy(isLoading = false, successMessage = "Declaración cancelada exitosamente.") }
                    syncDeclaracionesVentaUseCase()
                    syncStockUseCase() // Refrescar stock tras cancelar
                }
                is Result.Failure -> {
                    val msg = (result.error as? GenericError)?.message ?: "Error al cancelar"
                    _uiState.update { it.copy(isLoading = false, error = msg) }
                }
            }
        }
    }

    fun onUpSelected(upId: Int) {
        viewModelScope.launch {
            val catalogos = catalogosRepository.getMovimientoCatalogos().first()
            val especiesDisponibles = catalogos.especies

            _uiState.update { 
                it.copy(
                    selectedUpId = upId,
                    selectedEspecieId = null,
                    selectedRazaId = null,
                    selectedCategoriaId = null,
                    especies = especiesDisponibles,
                    razas = emptyList(),
                    categorias = emptyList()
                ) 
            }
        }
    }

    fun onEspecieSelected(especieId: Int) {
        viewModelScope.launch {
            val state = _uiState.value
            val catalogos = catalogosRepository.getMovimientoCatalogos().first()
            
            // Obtener todas las razas y categorías disponibles en el catálogo para esta especie
            // Eliminamos el filtrado restrictivo por stock para permitir la selección fluida
            val allRazas = catalogos.razas.filter { it.especieId == especieId }
            val allCategorias = catalogos.categorias.filter { it.especieId == especieId }

            _uiState.update { 
                it.copy(
                    selectedEspecieId = especieId,
                    selectedRazaId = null,
                    selectedCategoriaId = null,
                    razas = allRazas,
                    categorias = allCategorias
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

    fun onPesoAproximadoChanged(peso: String) {
        // Permitir solo números y un punto decimal
        if (peso.isEmpty() || peso.matches(Regex("^\\d*\\.?\\d*$"))) {
            _uiState.update { it.copy(pesoAproximado = peso) }
        }
    }

    fun onSubmit() {
        viewModelScope.launch {
            val state = _uiState.value
            val cantidadInt = state.cantidad.toIntOrNull()
            val pesoFloat = state.pesoAproximado.toFloatOrNull()

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
                        observaciones = state.observaciones,
                        pesoAproximadoKg = pesoFloat
                    )

                    when (result) {
                        is Result.Success -> {
                            _uiState.update { 
                                it.copy(
                                    isLoading = false, 
                                    successMessage = "Venta registrada correctamente.",
                                    // Limpiar formulario parcial
                                    cantidad = "",
                                    observaciones = "",
                                    pesoAproximado = ""
                                )
                            }
                            // 3. Sincronizar Stock y Declaraciones tras éxito
                            syncDeclaracionesVentaUseCase()
                            syncStockUseCase()
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
