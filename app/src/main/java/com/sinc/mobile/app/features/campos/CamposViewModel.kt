package com.sinc.mobile.app.features.campos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sinc.mobile.domain.model.UnidadProductiva
import com.sinc.mobile.domain.use_case.GetUnidadesProductivasUseCase
import com.sinc.mobile.domain.use_case.SyncUnidadesProductivasUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CamposUiState(
    val isLoading: Boolean = true,
    val unidades: List<UnidadProductiva> = emptyList()
)

@HiltViewModel
class CamposViewModel @Inject constructor(
    private val getUnidadesProductivasUseCase: GetUnidadesProductivasUseCase,
    private val syncUnidadesProductivasUseCase: SyncUnidadesProductivasUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(CamposUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            // Trigger initial sync
            syncUnidadesProductivas()

            // Observe local data
            getUnidadesProductivasUseCase().collect { unidades ->
                _uiState.update { it.copy(unidades = unidades, isLoading = false) }
            }
        }
    }

    fun syncUnidadesProductivas() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            syncUnidadesProductivasUseCase()
            // isLoading will be set to false by the collector when data arrives
        }
    }
}
