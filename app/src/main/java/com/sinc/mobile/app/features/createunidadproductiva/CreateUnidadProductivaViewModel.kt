package com.sinc.mobile.app.features.createunidadproductiva

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sinc.mobile.domain.model.Catalogos
import com.sinc.mobile.domain.model.CondicionTenencia
import com.sinc.mobile.domain.model.Municipio
import com.sinc.mobile.domain.model.Paraje
import com.sinc.mobile.domain.use_case.GetCatalogosUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateUnidadProductivaViewModel @Inject constructor(
    private val getCatalogosUseCase: GetCatalogosUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateUnidadProductivaState())
    val uiState: StateFlow<CreateUnidadProductivaState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            getCatalogosUseCase().collectLatest { catalogos ->
                _uiState.update { it.copy(catalogos = catalogos, isLoading = false) }
            }
        }
    }

    fun onNextStep() {
        // TODO: Add validation
        _uiState.update { currentState ->
            val nextStep = (currentState.currentStep + 1).coerceAtMost(3)
            currentState.copy(currentStep = nextStep)
        }
    }

    fun onPreviousStep() {
        _uiState.update { currentState ->
            val prevStep = (currentState.currentStep - 1).coerceAtLeast(1)
            currentState.copy(currentStep = prevStep)
        }
    }

    fun onStepSelected(step: Int) {
        _uiState.update { it.copy(currentStep = step) }
    }

    fun onNombreChange(nombre: String) {
        _uiState.update { it.copy(nombre = nombre) }
    }

    fun onIdentificadorLocalChange(identificador: String) {
        _uiState.update { it.copy(identificadorLocal = identificador) }
    }

    fun onSuperficieChange(superficie: String) {
        _uiState.update { it.copy(superficie = superficie) }
    }

    fun onMunicipioSelected(municipio: Municipio) {
        _uiState.update { it.copy(selectedMunicipio = municipio, selectedParaje = null) }
        // TODO: Filter parajes from uiState.catalogos
    }

    fun onParajeSelected(paraje: Paraje) {
        _uiState.update { it.copy(selectedParaje = paraje) }
    }

    fun onCondicionTenenciaSelected(condicion: CondicionTenencia) {
        _uiState.update { it.copy(selectedCondicionTenencia = condicion) }
    }

    fun onHabitaChange(habita: Boolean) {
        _uiState.update { it.copy(habita = habita) }
    }
}

data class CreateUnidadProductivaState(
    val currentStep: Int = 1,

    // Step 1 Fields
    val nombre: String = "",
    val identificadorLocal: String = "",
    val superficie: String = "",
    val selectedMunicipio: Municipio? = null,
    val selectedParaje: Paraje? = null,
    val selectedCondicionTenencia: CondicionTenencia? = null,
    val habita: Boolean = false,

    // Data
    val catalogos: Catalogos? = null,

    // Loading/Error states
    val isLoading: Boolean = false,
    val error: String? = null
)
