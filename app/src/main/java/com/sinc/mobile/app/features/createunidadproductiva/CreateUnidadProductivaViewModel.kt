package com.sinc.mobile.app.features.createunidadproductiva

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sinc.mobile.domain.model.CreateUnidadProductivaData
import com.sinc.mobile.domain.model.UnidadProductiva
import com.sinc.mobile.domain.use_case.CreateUnidadProductivaUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateUnidadProductivaViewModel @Inject constructor(
    private val createUnidadProductivaUseCase: CreateUnidadProductivaUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateUnidadProductivaUiState())
    val uiState: StateFlow<CreateUnidadProductivaUiState> = _uiState

    fun createUnidadProductiva(data: CreateUnidadProductivaData) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            val result = createUnidadProductivaUseCase(data)
            result.onSuccess { unidadProductiva ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    unidadProductivaCreated = unidadProductiva,
                    error = null
                )
            }.onFailure { throwable ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = throwable.message ?: "Error desconocido al crear unidad productiva"
                )
            }
        }
    }
}

data class CreateUnidadProductivaUiState(
    val isLoading: Boolean = false,
    val unidadProductivaCreated: UnidadProductiva? = null,
    val error: String? = null
)
