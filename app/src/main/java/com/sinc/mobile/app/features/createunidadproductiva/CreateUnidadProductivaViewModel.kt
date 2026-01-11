package com.sinc.mobile.app.features.createunidadproductiva

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sinc.mobile.app.features.createunidadproductiva.components.MapMode
import com.sinc.mobile.app.util.GeoUtils
import com.sinc.mobile.domain.model.CreateUnidadProductivaData
import com.sinc.mobile.domain.model.DomainGeoPoint
import com.sinc.mobile.domain.model.IdentifierConfig
import com.sinc.mobile.domain.model.LocationError
import com.sinc.mobile.domain.model.Municipio
import com.sinc.mobile.domain.repository.CatalogosRepository
import com.sinc.mobile.domain.use_case.CreateUnidadProductivaUseCase
import com.sinc.mobile.domain.use_case.GetCurrentLocationUseCase
import com.sinc.mobile.domain.use_case.GetIdentifierConfigsUseCase
import com.sinc.mobile.domain.use_case.SyncIdentifierConfigsUseCase
import com.sinc.mobile.domain.use_case.ticket.SubmitTicketUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import javax.inject.Inject

data class IdentifierFormatInfo(
    val pattern: String,
    val maxLength: Int
)

data class CreateUnidadProductivaState(
    val currentStep: Int = 1,
    val showExitDialog: Boolean = false,


    // Step 1 State
    val isMapVisible: Boolean = false,
    val mapMode: MapMode = MapMode.SEARCH_ON_MAP,
    val selectedLocation: DomainGeoPoint? = null,
    val locationError: LocationError? = null,
    val mapErrorMessage: String? = null,
    val showPermissionBottomSheet: Boolean = false,
    val animateToLocation: DomainGeoPoint? = null,
    val isFetchingLocation: Boolean = false,
    val municipios: List<Municipio> = emptyList(),
    val selectedMunicipio: Municipio? = null,

    // Step 2 State
    val nombre: String = "",
    val nombreError: String? = null,
    val identifierConfigs: List<IdentifierConfig> = emptyList(),
    val selectedIdentifierConfig: IdentifierConfig? = null,
    val identifierFormatInfo: IdentifierFormatInfo? = null,
    val identifierValue: String = "",
    val identifierError: String? = null,
    val superficie: String = "",
    val superficieError: String? = null,
    val showRnspaRequestModal: Boolean = false,
    val rnspaRequestMunicipio: String = "",
    val rnspaRequestParaje: String = "",
    val rnspaRequestDireccion: String = "",
    val rnspaRequestInfoAdicional: String = "",
    val rnspaRequestLoading: Boolean = false,
    val rnspaRequestResult: com.sinc.mobile.domain.util.Result<Unit, com.sinc.mobile.domain.util.Error>? = null,


    // Step 3 State
    val condicionTenencia: String = "",
    val condicionTenenciaError: String? = null,

    // Submission State
    val isSubmitting: Boolean = false,
    val submissionResult: com.sinc.mobile.domain.util.Result<com.sinc.mobile.domain.model.UnidadProductiva, com.sinc.mobile.domain.util.Error>? = null
)

@HiltViewModel
class CreateUnidadProductivaViewModel @Inject constructor(
    private val getCurrentLocationUseCase: GetCurrentLocationUseCase,
    private val catalogosRepository: CatalogosRepository,
    private val getIdentifierConfigsUseCase: GetIdentifierConfigsUseCase,
    private val syncIdentifierConfigsUseCase: SyncIdentifierConfigsUseCase,
    private val createUnidadProductivaUseCase: CreateUnidadProductivaUseCase,
    private val submitTicketUseCase: SubmitTicketUseCase
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
        viewModelScope.launch {
            syncIdentifierConfigsUseCase()
            combine(
                catalogosRepository.getCatalogos(),
                getIdentifierConfigsUseCase()
            ) { catalogos, identifierConfigs ->
                val selectedConfig = _uiState.value.selectedIdentifierConfig ?: identifierConfigs.firstOrNull()
                _uiState.update {
                    it.copy(
                        municipios = catalogos.municipios,
                        identifierConfigs = identifierConfigs,
                        selectedIdentifierConfig = selectedConfig,
                        identifierFormatInfo = selectedConfig?.let { c -> getFormatInfoFromRegex(c.regex) }
                    )
                }
            }.collect {}
        }
    }

    private fun getFormatInfoFromRegex(regex: String): IdentifierFormatInfo {
        var pattern = regex.removeSurrounding("^", "$")
        pattern = Regex("""\\d\{(\d+)\}""").replace(pattern) {
            "#".repeat(it.groupValues[1].toInt())
        }
        pattern = pattern.replace(Regex("""\\d"""), "#")
        pattern = pattern.replace(Regex("""\\(.)""")) {
            it.groupValues[1]
        }
        val maxLength = pattern.count { it == '#' }
        return IdentifierFormatInfo(pattern, maxLength)
    }

    fun onNextStep() {
        viewModelScope.launch {
            when (_uiState.value.currentStep) {
                1 -> {
                    // Validation for step 1 is handled implicitly by onMapLocationSelected
                    // We just need a placeholder here in case the UI changes
                }
                2 -> {
                    if (!validateStep2()) return@launch
                    _uiState.update { it.copy(currentStep = 3) }
                }
                3 -> {
                    if (!validateStep3()) return@launch
                    submitForm()
                }
            }
        }
    }

    private fun validateStep2(): Boolean {
        val state = _uiState.value
        val nombreError = if (state.nombre.isBlank()) "El nombre del campo es obligatorio." else null
        val superficieError = if (state.superficie.isBlank()) "La superficie es obligatoria." else if (state.superficie.toFloatOrNull() == null) "La superficie debe ser un número." else null
        val identifierConfig = state.selectedIdentifierConfig
        val formatInfo = state.identifierFormatInfo
        val identifierError = if (identifierConfig != null && formatInfo != null) {
            if (state.identifierValue.isBlank()) {
                "El campo ${identifierConfig.type.uppercase()} es obligatorio."
            } else if (state.identifierValue.length != formatInfo.maxLength) {
                "El ${identifierConfig.type.uppercase()} debe tener ${formatInfo.maxLength} dígitos."
            } else {
                null
            }
        } else {
            null
        }

        _uiState.update { it.copy(nombreError = nombreError, superficieError = superficieError, identifierError = identifierError) }
        return nombreError == null && superficieError == null && identifierError == null
    }

    private suspend fun validateStep3(): Boolean {
        val state = _uiState.value
        val catalogs = catalogosRepository.getCatalogos().first()
        val tenencia = catalogs.condicionesTenencia.firstOrNull { it.nombre == state.condicionTenencia }
        val tenenciaError = if (tenencia == null) "Debe seleccionar una condición de tenencia." else null

        _uiState.update { it.copy(condicionTenenciaError = tenenciaError) }
        return tenenciaError == null
    }

    private suspend fun submitForm() {
        _uiState.update { it.copy(isSubmitting = true, submissionResult = null) }

        val state = _uiState.value
        val catalogs = catalogosRepository.getCatalogos().first()

        val tenenciaId = catalogs.condicionesTenencia.firstOrNull { it.nombre == state.condicionTenencia }?.id

        // This check should ideally not be needed if validation is correct, but it's safe
        if (state.selectedLocation == null || state.selectedMunicipio == null || tenenciaId == null) {
            _uiState.update {
                it.copy(
                    isSubmitting = false,
                    submissionResult = com.sinc.mobile.domain.util.Result.Failure(
                        com.sinc.mobile.domain.model.GenericError("Faltan datos obligatorios. Por favor, revise los pasos anteriores.")
                    )
                )
            }
            return
        }

        val data = CreateUnidadProductivaData(
            nombre = state.nombre,
            identificadorLocal = state.identifierValue,
            superficie = state.superficie.toFloat(),
            latitud = state.selectedLocation.latitude.toFloat(),
            longitud = state.selectedLocation.longitude.toFloat(),
            municipioId = state.selectedMunicipio.id,
            condicionTenenciaId = tenenciaId,
            fuenteAguaId = null, // Optional, not in form
            tipoSueloId = null,  // Optional, not in form
            tipoPastoId = null   // Optional, not in form
        )

        val result = createUnidadProductivaUseCase(data)
        _uiState.update { it.copy(isSubmitting = false, submissionResult = result) }
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
            _uiState.update { it.copy(isFetchingLocation = true) }
            val result = getCurrentLocationUseCase()

            if (result is com.sinc.mobile.domain.util.Result.Success) {
                val newLocation = DomainGeoPoint(result.data.latitude, result.data.longitude)
                // JUST animate to the location. Do not validate here.
                _uiState.update { it.copy(animateToLocation = newLocation) }
            } else if (result is com.sinc.mobile.domain.util.Result.Failure) {
                // If getting the location fails, show a generic location error
                _uiState.update { it.copy(locationError = result.error) }
            }

            val duration = System.currentTimeMillis() - startTime
            if (duration < 2000) { kotlinx.coroutines.delay(2000 - duration) }
            _uiState.update { it.copy(isFetchingLocation = false) }
        }
    }

    fun onSearchOnMapClicked() {
        _uiState.update { it.copy(isMapVisible = true, mapMode = MapMode.SEARCH_ON_MAP) }
    }

    fun onMapDismissed() {
        _uiState.update { it.copy(isMapVisible = false, selectedMunicipio = null, mapErrorMessage = null) }
    }

    fun onMapAnimationCompleted() {
        _uiState.update { it.copy(animateToLocation = null) }
    }

    fun onMapLocationSelected(location: GeoPoint) {
        viewModelScope.launch {
            _uiState.update { it.copy(mapErrorMessage = null) }
            val domainGeoPoint = DomainGeoPoint(location.latitude, location.longitude)
            val foundMunicipio = findContainingMunicipio(domainGeoPoint)

                            if (foundMunicipio != null) {
                                _uiState.update {
                                    it.copy(
                                        selectedLocation = domainGeoPoint,
                                        selectedMunicipio = foundMunicipio,
                                        isMapVisible = false,
                                        mapErrorMessage = null
                                    )
                                }
                                _uiState.update { it.copy(currentStep = 2) }
                            } else {                _uiState.update {
                    it.copy(
                        mapErrorMessage = "La ubicación seleccionada está fuera de los municipios válidos.",
                        isMapVisible = true // Keep map visible to allow correction
                    )
                }
            }
        }
    }

    private fun findContainingMunicipio(point: DomainGeoPoint): Municipio? {
        val municipios = _uiState.value.municipios
        return municipios.firstOrNull { municipio ->
            municipio.poligono?.let { polygon ->
                GeoUtils.isPointInPolygon(point, polygon)
            } ?: false
        }
    }

    fun clearMapErrorMessage() {
        _uiState.update { it.copy(mapErrorMessage = null) }
    }

    fun onMunicipioSelected(municipio: Municipio) {
        _uiState.update {
            it.copy(
                selectedMunicipio = municipio,
                isMapVisible = true,
                animateToLocation = municipio.centroide,
                mapErrorMessage = null
            )
        }
    }

    // --- Step 2 Methods ---
    fun onNombreChange(newValue: String) {
        _uiState.update { it.copy(nombre = newValue, nombreError = null) }
    }

    fun onIdentifierValueChange(newValue: String) {
        val digitsOnly = newValue.filter { it.isDigit() }
        val maxLength = _uiState.value.identifierFormatInfo?.maxLength ?: Int.MAX_VALUE
        if (digitsOnly.length <= maxLength) {
            _uiState.update { it.copy(identifierValue = digitsOnly, identifierError = null) }
        }
    }

    fun onSuperficieChange(newValue: String) {
        _uiState.update { it.copy(superficie = newValue, superficieError = null) }
    }

    fun onShowRnspaRequestModal() {
        _uiState.update { it.copy(showRnspaRequestModal = true, rnspaRequestResult = null) }
    }

    fun onDismissRnspaRequestModal() {
        _uiState.update {
            it.copy(
                showRnspaRequestModal = false,
                rnspaRequestLoading = false,
                rnspaRequestMunicipio = "",
                rnspaRequestParaje = "",
                rnspaRequestDireccion = "",
                rnspaRequestInfoAdicional = "",
                rnspaRequestResult = null
            )
        }
    }

    fun onRnspaRequestMunicipioChange(value: String) {
        _uiState.update { it.copy(rnspaRequestMunicipio = value) }
    }
    fun onRnspaRequestParajeChange(value: String) {
        _uiState.update { it.copy(rnspaRequestParaje = value) }
    }
    fun onRnspaRequestDireccionChange(value: String) {
        _uiState.update { it.copy(rnspaRequestDireccion = value) }
    }
    fun onRnspaRequestInfoAdicionalChange(value: String) {
        _uiState.update { it.copy(rnspaRequestInfoAdicional = value) }
    }

    fun onSubmitRnspaRequest() {
        viewModelScope.launch {
            _uiState.update { it.copy(rnspaRequestLoading = true) }
            val state = _uiState.value
            val label = state.selectedIdentifierConfig?.type?.uppercase() ?: "Identificador"
            val mensaje = """
                Solicitud de número de $label.
                Datos proporcionados por el productor:
                - Municipio: ${state.rnspaRequestMunicipio}
                - Paraje: ${state.rnspaRequestParaje}
                - Dirección: ${state.rnspaRequestDireccion}
                Información adicional:
                ${state.rnspaRequestInfoAdicional}
            """.trimIndent()
            val result = submitTicketUseCase(
                mensaje = mensaje,
                tipo = "solicitud_rnspa"
            )
            _uiState.update { it.copy(rnspaRequestLoading = false, rnspaRequestResult = result) }
        }
    }

    // --- Step 3 Methods ---
    fun onCondicionTenenciaChange(newValue: String) {
        _uiState.update { it.copy(condicionTenencia = newValue, condicionTenenciaError = null) }
    }

    fun clearSubmissionResult() {
        _uiState.update { it.copy(submissionResult = null) }
    }
}