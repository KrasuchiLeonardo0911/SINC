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

import com.sinc.mobile.domain.model.Movimiento
import com.sinc.mobile.domain.use_case.SaveMovimientoUseCase
import com.sinc.mobile.domain.model.MotivoMovimiento

data class HomeState(
    val isLoading: Boolean = false,
    val unidades: List<UnidadProductiva> = emptyList(),
    val error: String? = null,
    val selectedUnidad: UnidadProductiva? = null,
    val isDropdownExpanded: Boolean = false,
    val isCatalogosLoading: Boolean = false,
    val catalogos: Catalogos? = null,

    val selectedEspecie: Especie? = null,
    val selectedCategoria: Categoria? = null,
    val selectedRaza: Raza? = null,
    val selectedMotivo: MotivoMovimiento? = null,
    val cantidad: String = "",
    val destino: String = "",

    val filteredCategorias: List<Categoria> = emptyList(),
    val filteredRazas: List<Raza> = emptyList(),

    val isFormValid: Boolean = false,
    val isSaving: Boolean = false,
    val saveError: String? = null,
    val saveSuccess: Boolean = false
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getUnidadesProductivasUseCase: GetUnidadesProductivasUseCase,
    private val getCatalogosUseCase: GetCatalogosUseCase,
    private val saveMovimientoUseCase: SaveMovimientoUseCase
) : ViewModel() {

    private val _state = mutableStateOf(HomeState())
    val state: State<HomeState> = _state

    init {
        loadUnidadesProductivas()
    }

    fun onUnidadSelected(unidad: UnidadProductiva) {
        _state.value = _state.value.copy(
            selectedUnidad = unidad,
            isDropdownExpanded = false,
            catalogos = null,
            selectedEspecie = null,
            selectedCategoria = null,
            selectedRaza = null,
            selectedMotivo = null,
            cantidad = "",
            destino = "",
            filteredCategorias = emptyList(),
            filteredRazas = emptyList(),
            isFormValid = false,
            saveError = null,
            saveSuccess = false
        )
        loadCatalogos()
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
        _state.value = _state.value.copy(destino = destino)
        validateForm()
    }

    private fun validateForm() {
        val state = _state.value
        val isDestinoRequired = state.selectedMotivo?.nombre?.contains("Traslado", ignoreCase = true) == true ||
                                state.selectedMotivo?.nombre?.contains("Venta", ignoreCase = true) == true ||
                                state.selectedMotivo?.nombre?.contains("Compra", ignoreCase = true) == true

        val isValid = state.selectedEspecie != null &&
                      state.selectedCategoria != null &&
                      state.selectedRaza != null &&
                      state.selectedMotivo != null &&
                      state.cantidad.isNotBlank() && state.cantidad.toIntOrNull() ?: 0 > 0 &&
                      (!isDestinoRequired || state.destino.isNotBlank())

        _state.value = _state.value.copy(isFormValid = isValid)
    }

    fun saveMovement() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSaving = true, saveError = null, saveSuccess = false)
            val state = _state.value
            val movimiento = Movimiento(
                especieId = state.selectedEspecie!!.id,
                categoriaId = state.selectedCategoria!!.id,
                razaId = state.selectedRaza!!.id,
                cantidad = state.cantidad.toInt(),
                motivoMovimientoId = state.selectedMotivo!!.id,
                destinoTraslado = if (state.selectedMotivo?.nombre?.contains("Traslado", ignoreCase = true) == true ||
                                     state.selectedMotivo?.nombre?.contains("Venta", ignoreCase = true) == true ||
                                     state.selectedMotivo?.nombre?.contains("Compra", ignoreCase = true) == true) state.destino else null
            )
            saveMovimientoUseCase(movimiento).onSuccess {
                _state.value = _state.value.copy(isSaving = false, saveSuccess = true)
                // Optionally reset form fields here
            }.onFailure {
                _state.value = _state.value.copy(isSaving = false, saveError = it.message ?: "Error al guardar movimiento")
            }
        }
    }

    fun loadUnidadesProductivas() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            getUnidadesProductivasUseCase().collect {
                _state.value = _state.value.copy(isLoading = false, unidades = it)
            }
        }
    }

    private fun loadCatalogos() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isCatalogosLoading = true)
            getCatalogosUseCase().collect {
                _state.value = _state.value.copy(isCatalogosLoading = false, catalogos = it)
            }
        }
    }
}
