package com.sinc.mobile.app.features.home

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        // The responsibility for collecting and refreshing stock data is now in StockViewModel.
    }

    fun resetNavigationToCreateUnidadProductiva() {
        _uiState.update { it.copy(shouldNavigateToCreateUnidadProductiva = false) }
    }
}

data class MainUiState(
    val shouldNavigateToCreateUnidadProductiva: Boolean = false
)