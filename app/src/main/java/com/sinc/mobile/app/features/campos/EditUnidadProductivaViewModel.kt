package com.sinc.mobile.app.features.campos

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sinc.mobile.domain.model.Catalogos
import com.sinc.mobile.domain.model.UnidadProductiva
import com.sinc.mobile.domain.model.UpdateUnidadProductivaData
import com.sinc.mobile.domain.use_case.GetCatalogosUseCase
import com.sinc.mobile.domain.use_case.GetUnidadesProductivasUseCase
import com.sinc.mobile.domain.use_case.SyncCatalogosUseCase
import com.sinc.mobile.domain.use_case.UpdateUnidadProductivaUseCase
import com.sinc.mobile.domain.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
    
    // Basic Info
    val superficie: String = "",
    
    // Land Data
    val condicionTenenciaId: Int? = null,
    val tipoSueloId: Int? = null,
    val tipoPastoId: Int? = null,
    
    // Water & Living
    val aguaHumanoFuenteId: Int? = null,
    val aguaHumanoEnCasa: Boolean = false,
    val aguaHumanoDistancia: String = "",
    val aguaAnimalFuenteId: Int? = null,
    val aguaAnimalDistancia: String = "",
    
    val forrajerasPredominante: Boolean = false,
    val habita: Boolean = false,
    
    val observaciones: String = ""
)

@HiltViewModel
class EditUnidadProductivaViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getUnidadesProductivasUseCase: GetUnidadesProductivasUseCase,
    getCatalogosUseCase: GetCatalogosUseCase,
    private val updateUnidadProductivaUseCase: UpdateUnidadProductivaUseCase,
    private val syncCatalogosUseCase: SyncCatalogosUseCase
) : ViewModel() {

    private val unidadId: Int = checkNotNull(savedStateHandle["unidadId"])

    private val _uiState = MutableStateFlow(EditUnidadProductivaState())
    val uiState: StateFlow<EditUnidadProductivaState> = _uiState

    init {
        val unidadFlow = getUnidadesProductivasUseCase()
        val catalogosFlow = getCatalogosUseCase()

        viewModelScope.launch {
            combine(unidadFlow, catalogosFlow) { unidades, catalogos ->
                val unidad = unidades.find { it.id == unidadId }
                Pair(unidad, catalogos)
            }.collect { (unidad, catalogos) ->
                if (unidad != null) {
                    _uiState.update { currentState ->
                        val isFirstLoad = currentState.unidad == null
                        
                        if (isFirstLoad) {
                            currentState.copy(
                                isLoading = false,
                                unidad = unidad,
                                catalogos = catalogos,
                                superficie = unidad.superficie?.toString() ?: "",
                                observaciones = unidad.observaciones ?: "",
                                condicionTenenciaId = unidad.condicionTenenciaId,
                                tipoSueloId = unidad.tipoSueloId,
                                tipoPastoId = unidad.tipoPastoId,
                                aguaHumanoFuenteId = unidad.aguaHumanoFuenteId,
                                aguaHumanoEnCasa = unidad.aguaHumanoEnCasa ?: false,
                                aguaHumanoDistancia = unidad.aguaHumanoDistancia?.toString() ?: "",
                                aguaAnimalFuenteId = unidad.aguaAnimalFuenteId,
                                aguaAnimalDistancia = unidad.aguaAnimalDistancia?.toString() ?: "",
                                forrajerasPredominante = unidad.forrajerasPredominante ?: false,
                                habita = unidad.habita ?: false
                            )
                        } else {
                            currentState.copy(
                                isLoading = false,
                                unidad = unidad,
                                catalogos = catalogos
                            )
                        }
                    }
                } else {
                     _uiState.update { it.copy(isLoading = false, error = "Unidad no encontrada") }
                }
            }
        }
        
        // Catalogs are synced smartly on app init
    }

    // Setters
    fun onSuperficieChange(v: String) = _uiState.update { it.copy(superficie = v) }
    fun onObservacionesChange(v: String) = _uiState.update { it.copy(observaciones = v) }
    
    fun onCondicionTenenciaChange(v: Int) = _uiState.update { it.copy(condicionTenenciaId = v) }
    fun onTipoSueloChange(v: Int) = _uiState.update { it.copy(tipoSueloId = v) }
    fun onTipoPastoChange(v: Int) = _uiState.update { it.copy(tipoPastoId = v) }
    
    fun onAguaHumanoFuenteChange(v: Int) = _uiState.update { it.copy(aguaHumanoFuenteId = v) }
    fun onAguaHumanoEnCasaChange(v: Boolean) = _uiState.update { it.copy(aguaHumanoEnCasa = v) }
    fun onAguaHumanoDistanciaChange(v: String) = _uiState.update { it.copy(aguaHumanoDistancia = v) }
    
    fun onAguaAnimalFuenteChange(v: Int) = _uiState.update { it.copy(aguaAnimalFuenteId = v) }
    fun onAguaAnimalDistanciaChange(v: String) = _uiState.update { it.copy(aguaAnimalDistancia = v) }
    
    fun onForrajerasChange(v: Boolean) = _uiState.update { it.copy(forrajerasPredominante = v) }
    fun onHabitaChange(v: Boolean) = _uiState.update { it.copy(habita = v) }

    fun saveChanges() {
        val s = _uiState.value
        val superficieD = s.superficie.toDoubleOrNull()
        val aguaHDist = s.aguaHumanoDistancia.toIntOrNull()
        val aguaADist = s.aguaAnimalDistancia.toIntOrNull()
        
        if (superficieD == null && s.superficie.isNotEmpty()) {
             _uiState.update { it.copy(error = "La superficie debe ser un número válido") }
             return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            
            val updateData = UpdateUnidadProductivaData(
                superficie = superficieD,
                condicionTenenciaId = s.condicionTenenciaId,
                aguaAnimalFuenteId = s.aguaAnimalFuenteId,
                aguaHumanoFuenteId = s.aguaHumanoFuenteId,
                aguaHumanoEnCasa = s.aguaHumanoEnCasa,
                aguaHumanoDistancia = aguaHDist,
                aguaAnimalDistancia = aguaADist,
                tipoSueloId = s.tipoSueloId,
                tipoPastoId = s.tipoPastoId,
                forrajerasPredominante = s.forrajerasPredominante,
                habita = s.habita,
                observaciones = s.observaciones
            )

            val result = updateUnidadProductivaUseCase(unidadId, updateData)
            
            when (result) {
                is Result.Success -> {
                    _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
                }
                is Result.Failure -> {
                    _uiState.update { it.copy(isSaving = false, error = result.error.message ?: "Error al guardar") }
                }
            }
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
