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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getUnidadesProductivasUseCase: GetUnidadesProductivasUseCase,
    private val syncUnidadesProductivasUseCase: SyncUnidadesProductivasUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        loadUnidadesProductivas()
    }

    private fun loadUnidadesProductivas() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) } // Clear previous errors

            // Primero, intentar sincronizar las UPs desde la API
            val syncResult = syncUnidadesProductivasUseCase()

            // Luego, observar las UPs desde la base de datos local
            getUnidadesProductivasUseCase().collectLatest { unidades ->
                val errorMsg = if (syncResult is com.sinc.mobile.domain.util.Result.Failure) {
                    (syncResult.error as? com.sinc.mobile.domain.model.GenericError)?.message
                } else null

                _uiState.update { it.copy(
                    isLoading = false,
                    unidadesProductivas = unidades,
                    error = errorMsg,
                    shouldNavigateToCreateUnidadProductiva = false // Always navigate to home, user can go to create UP manually
                ) }
            }
        }
    }

    fun resetNavigationToCreateUnidadProductiva() {
        _uiState.update { it.copy(shouldNavigateToCreateUnidadProductiva = false) }
    }
}

data class MainUiState(
    val isLoading: Boolean = false,
    val unidadesProductivas: List<UnidadProductiva> = emptyList(),
    val error: String? = null,
    val shouldNavigateToCreateUnidadProductiva: Boolean = false
)