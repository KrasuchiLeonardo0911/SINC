package com.sinc.mobile.app.features.createunidadproductiva

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class CreateUnidadProductivaState(
    val currentStep: Int = 1,
    val showExitDialog: Boolean = false,

    // Step 2 State
    val nombre: String = "",
    val rnspa: String = "",
    val superficie: String = "",

    // Step 3 State
    val condicionTenencia: String = ""
)

class CreateUnidadProductivaViewModel : ViewModel() {

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
