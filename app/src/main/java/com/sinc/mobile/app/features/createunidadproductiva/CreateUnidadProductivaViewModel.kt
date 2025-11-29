package com.sinc.mobile.app.features.createunidadproductiva

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sinc.mobile.app.features.createunidadproductiva.components.MapMode
import com.sinc.mobile.domain.model.DomainGeoPoint
import com.sinc.mobile.domain.model.LocationError
import com.sinc.mobile.domain.model.Municipio
import com.sinc.mobile.domain.repository.CatalogosRepository
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
    val mapMode: MapMode = MapMode.SEARCH_ON_MAP,
    val selectedLocation: DomainGeoPoint? = null,
    val locationError: LocationError? = null,
    val showPermissionBottomSheet: Boolean = false,
    val animateToLocation: DomainGeoPoint? = null,
    val isFetchingLocation: Boolean = false,
    val municipios: List<Municipio> = emptyList(),
    val selectedMunicipio: Municipio? = null,

    // Step 2 State
    val nombre: String = "",
    val rnspa: String = "",
    val superficie: String = "",

    // Step 3 State
    val condicionTenencia: String = ""
)

@HiltViewModel
class CreateUnidadProductivaViewModel @Inject constructor(
    private val getCurrentLocationUseCase: GetCurrentLocationUseCase,
    private val catalogosRepository: CatalogosRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateUnidadProductivaState())
    val uiState = _uiState.asStateFlow()

    val tenenciaOptions = listOf(
        "Propietario",
        "Arrendatario",
        "Aparcero",
        "Ocupante precario",
        "Otro"
    )

    init {
        loadMunicipios()
    }

    private fun loadMunicipios() {
        viewModelScope.launch {
            catalogosRepository.getCatalogos().collect { catalogos ->
                _uiState.update { it.copy(municipios = catalogos.municipios) }
            }
        }
    }

    fun onMunicipioSelected(municipio: Municipio) {
        _uiState.update {
            it.copy(
                selectedMunicipio = municipio,
                isMapVisible = true,
                animateToLocation = municipio.centroide
            )
        }
    }

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

    fun onUseCurrentLocationClicked() {
        _uiState.update {
            it.copy(
                locationError = null,
                showPermissionBottomSheet = true,
                mapMode = MapMode.CURRENT_LOCATION
            )
        }
    }

    fun onPermissionBottomSheetDismissed() {
        _uiState.update { it.copy(showPermissionBottomSheet = false) }
    }

    fun onPermissionResult(isGranted: Boolean) {
        if (isGranted) {
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

            if (result is Result.Success) {
                val newLocation = DomainGeoPoint(result.data.latitude, result.data.longitude)
                _uiState.update { it.copy(selectedLocation = newLocation) }
            } else if (result is Result.Failure) {
                _uiState.update {
                    it.copy(
                        locationError = result.error,
                        isFetchingLocation = false
                    )
                }
            }

            val duration = System.currentTimeMillis() - startTime
            if (duration < 2000) {
                kotlinx.coroutines.delay(2000 - duration)
            }

            if (_uiState.value.locationError == null) {
                _uiState.update {
                    it.copy(
                        animateToLocation = it.selectedLocation,
                        isFetchingLocation = false
                    )
                }
            }
        }
    }

    fun onSearchOnMapClicked() {
        _uiState.update {
            it.copy(
                isMapVisible = true,
                mapMode = MapMode.SEARCH_ON_MAP
            )
        }
    }

    fun onMapDismissed() {
        _uiState.update { it.copy(isMapVisible = false, selectedMunicipio = null) }
    }

    fun onMapAnimationCompleted() {
        _uiState.update { it.copy(animateToLocation = null) }
    }

    fun onMapLocationSelected(location: GeoPoint) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    selectedLocation = DomainGeoPoint(location.latitude, location.longitude),
                    isMapVisible = false
                )
            }
            kotlinx.coroutines.delay(500L)
            _uiState.update { it.copy(currentStep = 2) }
        }
    }

    fun onNombreChange(newValue: String) {
        _uiState.update { it.copy(nombre = newValue) }
    }

    fun onRnspaChange(newValue: String) {
        _uiState.update { it.copy(rnspa = newValue) }
    }

    fun onSuperficieChange(newValue: String) {
        _uiState.update { it.copy(superficie = newValue) }
    }

    fun onCondicionTenenciaChange(newValue: String) {
        _uiState.update { it.copy(condicionTenencia = newValue) }
    }
}