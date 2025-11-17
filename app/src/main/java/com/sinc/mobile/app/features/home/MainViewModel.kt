package com.sinc.mobile.app.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sinc.mobile.domain.model.UnidadProductiva
import com.sinc.mobile.domain.use_case.GetUnidadesProductivasUseCase
import com.sinc.mobile.domain.use_case.SyncUnidadesProductivasUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getUnidadesProductivasUseCase: GetUnidadesProductivasUseCase,
    private val syncUnidadesProductivasUseCase: SyncUnidadesProductivasUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val _shouldNavigateToCreateUnidadProductiva = MutableStateFlow(false)
    val shouldNavigateToCreateUnidadProductiva: StateFlow<Boolean> = _shouldNavigateToCreateUnidadProductiva.asStateFlow()

    init {
        loadUnidadesProductivas()
    }

    private fun loadUnidadesProductivas() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            // Primero, intentar sincronizar las UPs desde la API
            val syncResult = syncUnidadesProductivasUseCase()

            // Luego, observar las UPs desde la base de datos local
            getUnidadesProductivasUseCase().collectLatest { unidades ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    unidadesProductivas = unidades,
                    error = syncResult.exceptionOrNull()?.message
                )

                // Solo navegar si la sincronización falló y no hay UPs locales,
                // o si la sincronización fue exitosa y el resultado es una lista vacía.
                if (unidades.isEmpty()) {
                    _shouldNavigateToCreateUnidadProductiva.value = true
                }
            }
        }
    }

    fun resetNavigationToCreateUnidadProductiva() {
        _shouldNavigateToCreateUnidadProductiva.value = false
    }
}

data class MainUiState(
    val isLoading: Boolean = false,
    val unidadesProductivas: List<UnidadProductiva> = emptyList(),
    val error: String? = null
)