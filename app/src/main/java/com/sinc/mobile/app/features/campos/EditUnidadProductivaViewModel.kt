package com.sinc.mobile.app.features.campos

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sinc.mobile.domain.model.Catalogos
import com.sinc.mobile.domain.model.UnidadProductiva
import com.sinc.mobile.domain.model.UpdateUnidadProductivaData
import com.sinc.mobile.domain.use_case.GetCatalogosUseCase
import com.sinc.mobile.domain.use_case.GetUnidadesProductivasUseCase
import com.sinc.mobile.domain.use_case.UpdateUnidadProductivaUseCase
import com.sinc.mobile.domain.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
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
    
    // Form fields
    val superficie: String = "",
    val observaciones: String = "",
    val condicionTenenciaId: Int? = null,
    val fuenteAguaId: Int? = null
)

@HiltViewModel
class EditUnidadProductivaViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getUnidadesProductivasUseCase: GetUnidadesProductivasUseCase,
    getCatalogosUseCase: GetCatalogosUseCase,
    private val updateUnidadProductivaUseCase: UpdateUnidadProductivaUseCase
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
                        // Only update form fields if it's the first load or if unit changed significantly
                        // to avoid overwriting user input on background syncs
                        val isFirstLoad = currentState.unidad == null
                        
                        if (isFirstLoad) {
                            currentState.copy(
                                isLoading = false,
                                unidad = unidad,
                                catalogos = catalogos,
                                superficie = unidad.superficie?.toString() ?: "",
                                observaciones = unidad.observaciones ?: "",
                                condicionTenenciaId = unidad.condicionTenenciaId,
                                fuenteAguaId = unidad.fuenteAguaId // Use simple source ID if available directly or via pivot logic depending on backend structure. Assuming direct mapping for edit.
                            )
                        } else {
                            // Just update the reference data
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
    }

    fun onSuperficieChange(newValue: String) {
        _uiState.update { it.copy(superficie = newValue) }
    }

    fun onObservacionesChange(newValue: String) {
        _uiState.update { it.copy(observaciones = newValue) }
    }

    fun onCondicionTenenciaChange(newId: Int) {
        _uiState.update { it.copy(condicionTenenciaId = newId) }
    }
    
    fun onFuenteAguaChange(newId: Int) {
        _uiState.update { it.copy(fuenteAguaId = newId) }
    }

    fun saveChanges() {
        val currentState = _uiState.value
        val superficieDouble = currentState.superficie.toDoubleOrNull()
        
        if (superficieDouble == null && currentState.superficie.isNotEmpty()) {
             _uiState.update { it.copy(error = "La superficie debe ser un número válido") }
             return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            
            val updateData = UpdateUnidadProductivaData(
                superficie = superficieDouble,
                observaciones = currentState.observaciones,
                condicionTenenciaId = currentState.condicionTenenciaId,
                aguaAnimalFuenteId = currentState.fuenteAguaId
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
