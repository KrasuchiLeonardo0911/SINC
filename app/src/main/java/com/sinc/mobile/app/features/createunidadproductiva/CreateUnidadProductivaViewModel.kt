package com.sinc.mobile.app.features.createunidadproductiva

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sinc.mobile.domain.model.LocationError
import com.sinc.mobile.domain.use_case.GetCurrentLocationUseCase
import com.sinc.mobile.domain.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import javax.inject.Inject

data class CreateUnidadProductivaState(
    val currentStep: Int = 1,
    val showExitDialog: Boolean = false,

    // Step 1 State
    val isMapVisible: Boolean = false,
    val selectedLocation: GeoPoint? = null,
    val locationError: LocationError? = null,
    val showPermissionBottomSheet: Boolean = false,
    val animateToLocation: GeoPoint? = null,
    val isFetchingLocation: Boolean = false, // New state for loading indicator

    // Step 2 State
    val nombre: String = "",
    val rnspa: String = "",
    val superficie: String = "",

    // Step 3 State
    val condicionTenencia: String = ""
)

@HiltViewModel
class CreateUnidadProductivaViewModel @Inject constructor(
    private val getCurrentLocationUseCase: GetCurrentLocationUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateUnidadProductivaState())
    val uiState = _uiState.asStateFlow()

    // Hardcoded options for tenancy condition
    val tenenciaOptions = listOf(
        "Propietario",
        "Arrendatario",
        "Aparcero",
        "Ocupante precario",
        "Otro"
    )

    fun onNextStep() {
        if (_uiState.value.currentStep < 3) {
            _uiState.update { it.copy(currentStep = it.currentStep + 1) }
        }
    }

    fun onPreviousStep() {
        if (_uiState.value.currentStep > 1) {
            _uiState.update { it.copy(currentStep = it.currentStep - 1) }
        }
    }

    fun onExitRequest() {
        _uiState.update { it.copy(showExitDialog = true) }
    }

    fun onExitDialogDismiss() {
        _uiState.update { it.copy(showExitDialog = false) }
    }

    // --- Step 1 Handlers ---
    fun onUseCurrentLocationClicked() {
        _uiState.update { it.copy(locationError = null, showPermissionBottomSheet = true) } // Clear previous errors and show sheet
    }

    fun onPermissionBottomSheetDismissed() {
        _uiState.update { it.copy(showPermissionBottomSheet = false) }
    }

    fun onPermissionResult(isGranted: Boolean) {
        if (isGranted) {
            // Show map centered on Misiones with loading indicator
            _uiState.update { it.copy(isMapVisible = true, isFetchingLocation = true) }
            fetchCurrentLocation()
        } else {
            _uiState.update { it.copy(locationError = LocationError.PermissionDenied) }
        }
    }

    fun fetchCurrentLocation() {
        viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            val result = getCurrentLocationUseCase()
            val duration = System.currentTimeMillis() - startTime

            if (duration < 2000) { // Increased delay to 2 seconds
                kotlinx.coroutines.delay(2000 - duration)
            }

            when (result) {
                is Result.Success -> {
                    val newLocation = GeoPoint(result.data.latitude, result.data.longitude)
                    _uiState.update {
                        it.copy(
                            selectedLocation = newLocation,
                            animateToLocation = newLocation, // Re-enabled animation
                            isFetchingLocation = false // Hide loading
                        )
                    }
                }
                is Result.Failure -> {
                    _uiState.update {
                        it.copy(
                            locationError = result.error,
                            isFetchingLocation = false // Hide loading
                        )
                    }
                }
            }
        }
    }

    fun onSearchOnMapClicked() {
        _uiState.update { it.copy(isMapVisible = true) }
    }

    fun onMapDismissed() {
        _uiState.update { it.copy(isMapVisible = false) }
    }

    fun onMapAnimationCompleted() {
        _uiState.update { it.copy(animateToLocation = null) }
    }

    fun onMapLocationSelected(location: GeoPoint) {
        _uiState.update { it.copy(selectedLocation = location, isMapVisible = false) }
    }

    // --- Step 2 Handlers ---
    fun onNombreChange(newValue: String) {
        _uiState.update { it.copy(nombre = newValue) }
    }

    fun onRnspaChange(newValue: String) {
        _uiState.update { it.copy(rnspa = newValue) }
    }

    fun onSuperficieChange(newValue: String) {
        _uiState.update { it.copy(superficie = newValue) }
    }

    // --- Step 3 Handlers ---
    fun onCondicionTenenciaChange(newValue: String) {
        _uiState.update { it.copy(condicionTenencia = newValue) }
    }
}
