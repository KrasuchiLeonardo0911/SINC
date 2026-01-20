package com.sinc.mobile.app.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sinc.mobile.domain.model.AppControl
import com.sinc.mobile.domain.model.Features
import com.sinc.mobile.domain.use_case.init.InitializeAppUseCase
import com.sinc.mobile.domain.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val initializeAppUseCase: InitializeAppUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        initializeApp()
    }

    private fun initializeApp() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Force minimum splash duration of 1 second for better UX
            val minDelay = async { delay(1000) }
            
            val result = initializeAppUseCase()

            // Wait for the minimum delay to finish
            minDelay.await()
            
            when (result) {
                is Result.Success -> {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            appControl = result.data.appControl,
                            features = result.data.features,
                            isInitialized = true
                        ) 
                    }
                }
                is Result.Failure -> {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "Error al conectar con el servidor",
                            isInitialized = true 
                        ) 
                    }
                }
            }
        }
    }

    fun resetNavigationToCreateUnidadProductiva() {
        _uiState.update { it.copy(shouldNavigateToCreateUnidadProductiva = false) }
    }
}

data class MainUiState(
    val isLoading: Boolean = true,
    val isInitialized: Boolean = false,
    val error: String? = null,
    val shouldNavigateToCreateUnidadProductiva: Boolean = false,
    val appControl: AppControl? = null,
    val features: Features? = null
)
