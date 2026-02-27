package com.sinc.mobile.app.features.weather

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sinc.mobile.domain.model.WeatherAlert
import com.sinc.mobile.domain.use_case.weather.GetWeatherAlertsUseCase
import com.sinc.mobile.domain.use_case.weather.SyncWeatherAlertsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WeatherUiState(
    val alerts: List<WeatherAlert> = emptyList(),
    val isInitialLoad: Boolean = true,
    val isLoading: Boolean = false,
    val selectedLayer: WindyLayer = WindyLayer.RADAR,
)

enum class WindyLayer(val path: String, val label: String) {
    RADAR("radar", "Radar"),
    RAIN("rain", "Lluvias"),
    WIND("wind", "Viento"),
    CLOUDS("clouds", "Nubes"),
    TEMPERATURE("temp", "Temp.")
}

sealed class WeatherEvent {
    data class NavigateToMap(val layer: String) : WeatherEvent()
}

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val getWeatherAlertsUseCase: GetWeatherAlertsUseCase,
    private val syncWeatherAlertsUseCase: SyncWeatherAlertsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(WeatherUiState())
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    private val _eventFlow = Channel<WeatherEvent>()
    val eventFlow = _eventFlow.receiveAsFlow()

    init {
        Log.d("WeatherVM", "Iniciando WeatherViewModel...")
        observeAlerts()
        initialSync()
    }

    private fun initialSync() {
        viewModelScope.launch {
            _uiState.update { it.copy(isInitialLoad = true) }
            val startTime = System.currentTimeMillis()
            
            syncWeatherAlertsUseCase()
            
            val duration = System.currentTimeMillis() - startTime
            if (duration < 1500) {
                delay(1500 - duration)
            }
            _uiState.update { it.copy(isInitialLoad = false) }
        }
    }

    private fun observeAlerts() {
        Log.d("WeatherVM", "Observando alertas desde la DB...")
        viewModelScope.launch {
            getWeatherAlertsUseCase().collect { alerts ->
                Log.d("WeatherVM", "Alertas recibidas de la DB: ${alerts.size}")
                _uiState.update { it.copy(alerts = alerts) }
            }
        }
    }

    fun refreshAlerts() {
        Log.d("WeatherVM", "Refrescando alertas desde la API...")
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = syncWeatherAlertsUseCase()
            Log.d("WeatherVM", "Resultado de sincronización: $result")
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun onLayerSelected(layer: WindyLayer) {
        _uiState.update { it.copy(selectedLayer = layer) }
        viewModelScope.launch {
            _eventFlow.send(WeatherEvent.NavigateToMap(layer.path))
        }
    }
}
