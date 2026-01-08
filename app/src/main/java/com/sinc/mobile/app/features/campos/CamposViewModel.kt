package com.sinc.mobile.app.features.campos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CamposUiState(
    val isLoading: Boolean = true
)

@HiltViewModel
class CamposViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(CamposUiState())
    val uiState = _uiState.asStateFlow()

    init {
        // Simulate loading for transition feel
        viewModelScope.launch {
            delay(400)
            _uiState.update { it.copy(isLoading = false) }
        }
    }
}
