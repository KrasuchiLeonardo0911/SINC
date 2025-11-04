package com.sinc.mobile.app.features.home

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sinc.mobile.domain.model.UnidadProductiva
import com.sinc.mobile.domain.use_case.GetUnidadesProductivasUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.sinc.mobile.domain.model.Catalogos
import com.sinc.mobile.domain.use_case.GetCatalogosUseCase
import com.sinc.mobile.domain.model.Categoria
import com.sinc.mobile.domain.model.Especie
import com.sinc.mobile.domain.model.Raza
import java.time.LocalDateTime
import com.sinc.mobile.domain.model.MovimientoPendiente
import com.sinc.mobile.domain.use_case.GetMovimientosPendientesUseCase
import com.sinc.mobile.domain.use_case.SaveMovimientoLocalUseCase
import com.sinc.mobile.domain.use_case.SyncMovimientosPendientesUseCase
import com.sinc.mobile.domain.model.MotivoMovimiento
import com.sinc.mobile.domain.use_case.DeleteMovimientoLocalUseCase
import kotlinx.coroutines.flow.collectLatest

// Data class to represent a grouped movement for the UI
data class MovimientoAgrupado(
    val unidadProductivaId: Int,
    val especieId: Int,
    val categoriaId: Int,
    val razaId: Int,
    val motivoMovimientoId: Int,
    val cantidadTotal: Int,
    val originales: List<MovimientoPendiente> // Keep original items for deletion
)

private data class MovimientoGroupKey(
    val unidadProductivaId: Int,
    val especieId: Int,
    val categoriaId: Int,
    val razaId: Int,
    val motivoMovimientoId: Int
)

sealed class FormStep {
    object UnidadProductivaSelection : FormStep()
    object ActionSelection : FormStep()
    object MovimientoForm : FormStep()
}

data class HomeState(
    // General State
    val isLoading: Boolean = true,
    val error: String? = null,
    val currentStep: FormStep = FormStep.UnidadProductivaSelection,

    // Data
    val unidades: List<UnidadProductiva> = emptyList(),
    val catalogos: Catalogos? = null,
    val movimientosAgrupados: List<MovimientoAgrupado> = emptyList(), // Changed from movimientosPendientes

    // Form Selections
    val selectedUnidad: UnidadProductiva? = null,
    val selectedAction: String? = null, // "alta" or "baja"
    val selectedEspecie: Especie? = null,
    val selectedCategoria: Categoria? = null,
    val selectedRaza: Raza? = null,
    val selectedMotivo: MotivoMovimiento? = null,
    val cantidad: String = "",
    val destino: String = "",

    // Filtered Lists for Dropdowns
    val filteredCategorias: List<Categoria> = emptyList(),
    val filteredRazas: List<Raza> = emptyList(),
    val filteredMotivos: List<MotivoMovimiento> = emptyList(),

    // UI State
    val isDropdownExpanded: Boolean = false,
    val isFormValid: Boolean = false,
    val isSaving: Boolean = false,
    val saveError: String? = null,
    val isSyncing: Boolean = false,
    val syncError: String? = null,
    val syncSuccess: Boolean = false,
    val isFormLoading: Boolean = false
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getUnidadesProductivasUseCase: GetUnidadesProductivasUseCase,
    private val getCatalogosUseCase: GetCatalogosUseCase,
    private val getMovimientosPendientesUseCase: GetMovimientosPendientesUseCase,
    private val saveMovimientoLocalUseCase: SaveMovimientoLocalUseCase,
    private val syncMovimientosPendientesUseCase: SyncMovimientosPendientesUseCase,
    private val deleteMovimientoLocalUseCase: DeleteMovimientoLocalUseCase
) : ViewModel() {

    private val _state = mutableStateOf(HomeState())
    val state: State<HomeState> = _state

    init {
        loadInitialData()
    }

    fun loadInitialData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            getCatalogosUseCase().collectLatest { catalogos ->
                _state.value = _state.value.copy(catalogos = catalogos)
                loadAndGroupMovimientosPendientes()
            }
        }
        viewModelScope.launch {
            getUnidadesProductivasUseCase().collectLatest { unidades ->
                _state.value = _state.value.copy(unidades = unidades, isLoading = false)
            }
        }
    }

    private fun loadAndGroupMovimientosPendientes() {
        viewModelScope.launch {
            getMovimientosPendientesUseCase().collect { movimientos ->
                val agrupados = movimientos
                    .groupBy {
                        MovimientoGroupKey(
                            unidadProductivaId = it.unidadProductivaId,
                            especieId = it.especieId,
                            categoriaId = it.categoriaId,
                            razaId = it.razaId,
                            motivoMovimientoId = it.motivoMovimientoId
                        )
                    }
                    .map { (_, group) ->
                        val first = group.first()
                        MovimientoAgrupado(
                            unidadProductivaId = first.unidadProductivaId,
                            especieId = first.especieId,
                            categoriaId = first.categoriaId,
                            razaId = first.razaId,
                            motivoMovimientoId = first.motivoMovimientoId,
                            cantidadTotal = group.sumOf { it.cantidad },
                            originales = group
                        )
                    }
                _state.value = _state.value.copy(movimientosAgrupados = agrupados)
            }
        }
    }

    fun onUnidadSelected(unidad: UnidadProductiva) {
        _state.value = _state.value.copy(
            selectedUnidad = unidad,
            isDropdownExpanded = false,
            currentStep = FormStep.ActionSelection,
            selectedAction = null,
            selectedEspecie = null,
            selectedCategoria = null,
            selectedRaza = null,
            selectedMotivo = null,
            cantidad = "",
            destino = "",
            isFormValid = false,
            saveError = null
        )
    }

    fun onActionSelected(action: String) {
        viewModelScope.launch {
            // 1. Activa el esqueleto y resetea el estado del formulario
            _state.value = _state.value.copy(
                isFormLoading = true,
                selectedAction = action,
                currentStep = FormStep.MovimientoForm,
                selectedEspecie = null,
                selectedCategoria = null,
                selectedRaza = null,
                selectedMotivo = null,
                cantidad = "",
                destino = ""
            )

            // 2. Espera para dar feedback visual
            kotlinx.coroutines.delay(500)

            // 3. Desactiva el esqueleto y filtra los datos necesarios
            val filteredMotivos = _state.value.catalogos?.motivosMovimiento?.filter {
                it.tipo.equals(action, ignoreCase = true)
            } ?: emptyList()

            _state.value = _state.value.copy(
                filteredMotivos = filteredMotivos,
                isFormLoading = false
            )
        }
    }

    fun onDropdownExpandedChange(isExpanded: Boolean) {
        _state.value = _state.value.copy(isDropdownExpanded = isExpanded)
    }

    fun onEspecieSelected(especie: Especie) {
        val filteredCategorias = _state.value.catalogos?.categorias?.filter {
            it.especieId == especie.id
        } ?: emptyList()
        val filteredRazas = _state.value.catalogos?.razas?.filter {
            it.especieId == especie.id
        } ?: emptyList()

        _state.value = _state.value.copy(
            selectedEspecie = especie,
            filteredCategorias = filteredCategorias,
            filteredRazas = filteredRazas,
            selectedCategoria = null,
            selectedRaza = null
        )
        validateForm()
    }

    fun onCategoriaSelected(categoria: Categoria) {
        _state.value = _state.value.copy(selectedCategoria = categoria)
        validateForm()
    }

    fun onRazaSelected(raza: Raza) {
        _state.value = _state.value.copy(selectedRaza = raza)
        validateForm()
    }

    fun onMotivoSelected(motivo: MotivoMovimiento) {
        _state.value = _state.value.copy(selectedMotivo = motivo)
        validateForm()
    }

    fun onCantidadChanged(cantidad: String) {
        if (cantidad.all { it.isDigit() }) {
            _state.value = _state.value.copy(cantidad = cantidad)
            validateForm()
        }
    }

    fun onDestinoChanged(destino: String) {
        if (destino.length <= 255) { // Assuming a max length for destino
            _state.value = _state.value.copy(destino = destino)
            validateForm()
        }
    }

    private fun validateForm() {
        val s = _state.value
        val isDestinoRequired = s.selectedMotivo?.nombre?.contains("Traslado", ignoreCase = true) == true ||
                s.selectedMotivo?.nombre?.contains("Venta", ignoreCase = true) == true ||
                s.selectedMotivo?.nombre?.contains("Compra", ignoreCase = true) == true

        val isValid = s.selectedEspecie != null &&
                s.selectedCategoria != null &&
                s.selectedRaza != null &&
                s.selectedMotivo != null &&
                s.cantidad.isNotBlank() && s.cantidad.toIntOrNull() ?: 0 > 0 &&
                (!isDestinoRequired || s.destino.isNotBlank())

        _state.value = _state.value.copy(isFormValid = isValid)
    }

    fun saveMovement() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSaving = true, saveError = null)
            val s = _state.value
            val movimiento = MovimientoPendiente(
                id = 0, // Room will auto-generate
                unidadProductivaId = s.selectedUnidad!!.id,
                especieId = s.selectedEspecie!!.id,
                categoriaId = s.selectedCategoria!!.id,
                razaId = s.selectedRaza!!.id,
                cantidad = s.cantidad.toInt(),
                motivoMovimientoId = s.selectedMotivo!!.id,
                destinoTraslado = if (s.selectedMotivo?.nombre?.contains("Traslado", ignoreCase = true) == true ||
                    s.selectedMotivo?.nombre?.contains("Venta", ignoreCase = true) == true ||
                    s.selectedMotivo?.nombre?.contains("Compra", ignoreCase = true) == true) s.destino else null,
                observaciones = null,
                fechaRegistro = LocalDateTime.now(),
                sincronizado = false
            )
            saveMovimientoLocalUseCase(movimiento).onSuccess {
                _state.value = _state.value.copy(
                    isSaving = false,
                    currentStep = FormStep.ActionSelection,
                    selectedAction = null, // Oculta el formulario
                    selectedEspecie = null,
                    selectedCategoria = null,
                    selectedRaza = null,
                    selectedMotivo = null,
                    cantidad = "",
                    destino = "",
                    isFormValid = false,
                    saveError = null
                )
            }.onFailure {
                _state.value = _state.value.copy(isSaving = false, saveError = it.message ?: "Error al guardar movimiento")
            }
        }
    }

    fun deleteMovimientoGroup(grupo: MovimientoAgrupado) {
        viewModelScope.launch {
            grupo.originales.forEach { movimiento ->
                deleteMovimientoLocalUseCase(movimiento).onFailure {
                    // Optionally handle error for single deletion failure
                }
            }
        }
    }

    fun syncMovements() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSyncing = true, syncError = null, syncSuccess = false)
            syncMovimientosPendientesUseCase().onSuccess {
                _state.value = _state.value.copy(isSyncing = false, syncSuccess = true)
                kotlinx.coroutines.delay(2000L)
                _state.value = _state.value.copy(syncSuccess = false)
            }.onFailure {
                android.util.Log.e("SyncError", "Fallo al sincronizar movimientos", it)
                _state.value = _state.value.copy(isSyncing = false, syncError = it.message ?: "Error al sincronizar")
                kotlinx.coroutines.delay(3000L)
                _state.value = _state.value.copy(syncError = null)
            }
        }
    }

    fun resetForm() {
        _state.value = _state.value.copy(
            currentStep = FormStep.UnidadProductivaSelection,
            selectedUnidad = null,
            selectedAction = null,
            selectedEspecie = null,
            selectedCategoria = null,
            selectedRaza = null,
            selectedMotivo = null,
            cantidad = "",
            destino = "",
            isFormValid = false,
            saveError = null
        )
    }
}