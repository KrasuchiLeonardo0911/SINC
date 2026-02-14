package com.sinc.mobile.app.features.campos

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sinc.mobile.domain.model.*
import com.sinc.mobile.domain.use_case.SyncCatalogosUseCase
import com.sinc.mobile.domain.use_case.UpdateUnidadProductivaUseCase
import com.sinc.mobile.domain.use_case.up.GetUnidadProductivaByIdUseCase
import com.sinc.mobile.domain.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditUnidadProductivaState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val unidad: UnidadProductiva? = null,
    val catalogos: Catalogos? = null,
    val error: String? = null,
    val saveSuccess: Boolean = false,

    // Editable fields
    val superficie: String = "",
    val condicionTenenciaId: Int? = null,
    val aguaHumanoFuenteId: Int? = null,
    val aguaHumanoEnCasa: Boolean = false,
    val aguaHumanoDistancia: String = "",
    val aguaAnimalFuenteId: Int? = null,
    val aguaAnimalDistancia: String = "",
    val habita: Boolean = false,
    val observaciones: String = "",
    val tiposSuelo: List<SueloInfo> = emptyList(),
    val recursosForrajeros: List<PastoInfo> = emptyList(),
)

@HiltViewModel
class EditUnidadProductivaViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getUnidadProductivaByIdUseCase: GetUnidadProductivaByIdUseCase,
    private val updateUnidadProductivaUseCase: UpdateUnidadProductivaUseCase,
    private val syncCatalogosUseCase: SyncCatalogosUseCase,
    private val getCatalogosUseCase: com.sinc.mobile.domain.use_case.GetCatalogosUseCase
) : ViewModel() {

    private val unidadId: Int = checkNotNull(savedStateHandle["unidadId"])

    private val _uiState = MutableStateFlow(EditUnidadProductivaState())
    val uiState: StateFlow<EditUnidadProductivaState> = _uiState

    init {
        viewModelScope.launch {
            // Ensure catalogs are fresh before observing
            syncCatalogosUseCase()

            // Combine flows to get both unidad and catalogos at once
            getUnidadProductivaByIdUseCase(unidadId).combine(getCatalogosUseCase()) { unidad, catalogos ->
                Pair(unidad, catalogos)
            }.collect { (unidad, catalogos) ->
                if (unidad != null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            unidad = unidad,
                            catalogos = catalogos, // Populate catalogos
                            superficie = unidad.superficie?.toString() ?: "",
                            condicionTenenciaId = unidad.condicionTenenciaId,
                            aguaHumanoFuenteId = unidad.aguaHumanoFuenteId,
                            aguaHumanoEnCasa = unidad.aguaHumanoEnCasa ?: false,
                            aguaHumanoDistancia = unidad.aguaHumanoDistancia?.toString() ?: "",
                            aguaAnimalFuenteId = unidad.aguaAnimalFuenteId,
                            aguaAnimalDistancia = unidad.aguaAnimalDistancia?.toString() ?: "",
                            habita = unidad.habita ?: false,
                            observaciones = unidad.observaciones ?: "",
                            tiposSuelo = unidad.tiposSuelo.toList(),
                            recursosForrajeros = unidad.recursosForrajeros.toList()
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Unidad no encontrada") }
                }
            }
        }
    }

    // Event Handlers
    fun onSuperficieChange(v: String) = _uiState.update { it.copy(superficie = v) }
    fun onObservacionesChange(v: String) = _uiState.update { it.copy(observaciones = v) }
    fun onCondicionTenenciaChange(id: Int) = _uiState.update { it.copy(condicionTenenciaId = id) }
    fun onAguaHumanoFuenteChange(id: Int) = _uiState.update { it.copy(aguaHumanoFuenteId = id) }
    fun onAguaHumanoEnCasaChange(v: Boolean) = _uiState.update { it.copy(aguaHumanoEnCasa = v) }
    fun onAguaHumanoDistanciaChange(v: String) = _uiState.update { it.copy(aguaHumanoDistancia = v) }
    fun onAguaAnimalFuenteChange(id: Int) = _uiState.update { it.copy(aguaAnimalFuenteId = id) }
    fun onAguaAnimalDistanciaChange(v: String) = _uiState.update { it.copy(aguaAnimalDistancia = v) }
    fun onHabitaChange(v: Boolean) = _uiState.update { it.copy(habita = v) }

    // Suelo Handlers
    fun addOrUpdateSuelo(suelo: SueloInfo) {
        _uiState.update { state ->
            val existing = state.tiposSuelo.any { it.id == suelo.id }
            val suelos = if (existing) {
                state.tiposSuelo.map { if (it.id == suelo.id) suelo else it }
            } else {
                state.tiposSuelo + suelo
            }
            state.copy(tiposSuelo = suelos)
        }
    }

    fun deleteSuelo(sueloId: Int) {
        _uiState.update { state ->
            state.copy(tiposSuelo = state.tiposSuelo.filterNot { it.id == sueloId })
        }
    }

    // Pasto Handlers
    fun addOrUpdatePasto(pasto: PastoInfo) {
        _uiState.update { state ->
            val existing = state.recursosForrajeros.any { it.id == pasto.id }
            val pastos = if (existing) {
                state.recursosForrajeros.map { if (it.id == pasto.id) pasto else it }
            } else {
                state.recursosForrajeros + pasto
            }
            state.copy(recursosForrajeros = pastos)
        }
    }

    fun deletePasto(pastoId: Int) {
        _uiState.update { state ->
            state.copy(recursosForrajeros = state.recursosForrajeros.filterNot { it.id == pastoId })
        }
    }

    fun saveChanges() {
        val s = _uiState.value
        
        // Validate all if saving everything
        if (s.tiposSuelo.isNotEmpty() && s.tiposSuelo.sumOf { it.porcentaje } != 100) {
            _uiState.update { it.copy(error = "La suma de porcentajes de suelos debe ser 100%") }
            return
        }
        if (s.recursosForrajeros.isNotEmpty() && s.recursosForrajeros.sumOf { it.porcentaje ?: 0 } != 100) {
            _uiState.update { it.copy(error = "La suma de porcentajes de forrajes debe ser 100%") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            
            val updateData = UpdateUnidadProductivaData(
                superficie = s.superficie.toDoubleOrNull(),
                condicionTenenciaId = s.condicionTenenciaId,
                aguaAnimalFuenteId = s.aguaAnimalFuenteId,
                aguaHumanoFuenteId = s.aguaHumanoFuenteId,
                aguaHumanoEnCasa = s.aguaHumanoEnCasa,
                aguaHumanoDistancia = s.aguaHumanoDistancia.toIntOrNull(),
                aguaAnimalDistancia = s.aguaAnimalDistancia.toIntOrNull(),
                habita = s.habita,
                observaciones = s.observaciones,
                tiposSuelo = s.tiposSuelo,
                recursosForrajeros = s.recursosForrajeros
            )

            handleSaveResult(updateUnidadProductivaUseCase(unidadId, updateData))
        }
    }

    fun saveSuelos() {
        val s = _uiState.value
        if (s.tiposSuelo.isNotEmpty() && s.tiposSuelo.sumOf { it.porcentaje } != 100) {
            _uiState.update { it.copy(error = "La suma de porcentajes de suelos debe ser 100%") }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            val updateData = UpdateUnidadProductivaData(tiposSuelo = s.tiposSuelo)
            handleSaveResult(updateUnidadProductivaUseCase(unidadId, updateData))
        }
    }

    fun savePastos() {
        val s = _uiState.value
        val sumPastos = s.recursosForrajeros.sumOf { it.porcentaje ?: 0 }
        if (s.recursosForrajeros.isNotEmpty() && sumPastos != 100) {
            _uiState.update { it.copy(error = "La suma de porcentajes de forrajes debe ser 100%") }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            val updateData = UpdateUnidadProductivaData(recursosForrajeros = s.recursosForrajeros)
            handleSaveResult(updateUnidadProductivaUseCase(unidadId, updateData))
        }
    }

    fun saveBasicInfo() {
        val s = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            val updateData = UpdateUnidadProductivaData(
                superficie = s.superficie.toDoubleOrNull(),
                condicionTenenciaId = s.condicionTenenciaId,
                habita = s.habita
            )
            handleSaveResult(updateUnidadProductivaUseCase(unidadId, updateData))
        }
    }

    fun saveWaterInfo() {
        val s = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            val updateData = UpdateUnidadProductivaData(
                aguaHumanoFuenteId = s.aguaHumanoFuenteId,
                aguaHumanoEnCasa = s.aguaHumanoEnCasa,
                aguaHumanoDistancia = s.aguaHumanoDistancia.toIntOrNull(),
                aguaAnimalFuenteId = s.aguaAnimalFuenteId,
                aguaAnimalDistancia = s.aguaAnimalDistancia.toIntOrNull()
            )
            handleSaveResult(updateUnidadProductivaUseCase(unidadId, updateData))
        }
    }

    fun saveObservations() {
        val s = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            val updateData = UpdateUnidadProductivaData(
                observaciones = s.observaciones
            )
            handleSaveResult(updateUnidadProductivaUseCase(unidadId, updateData))
        }
    }

    private fun handleSaveResult(result: Result<UnidadProductiva, com.sinc.mobile.domain.util.Error>) {
        when (result) {
            is Result.Success -> {
                _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
            }
            is Result.Failure -> {
                _uiState.update { it.copy(isSaving = false, error = result.error.message ?: "Error al guardar") }
            }
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun resetSaveSuccess() {
        _uiState.update { it.copy(saveSuccess = false) }
    }
}