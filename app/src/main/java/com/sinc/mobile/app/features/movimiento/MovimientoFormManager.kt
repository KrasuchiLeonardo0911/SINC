package com.sinc.mobile.app.features.movimiento

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.sinc.mobile.domain.model.Categoria
import com.sinc.mobile.domain.model.Especie
import com.sinc.mobile.domain.model.MotivoMovimiento
import com.sinc.mobile.domain.model.Raza
import com.sinc.mobile.domain.model.Catalogos

data class MovimientoFormState(
    val selectedEspecie: Especie? = null,
    val selectedCategoria: Categoria? = null,
    val selectedRaza: Raza? = null,
    val selectedMotivo: MotivoMovimiento? = null,
    val cantidad: String = "",
    val destino: String = "",

    val filteredEspecies: List<Especie> = emptyList(),
    val filteredCategorias: List<Categoria> = emptyList(),
    val filteredRazas: List<Raza> = emptyList(),
    val filteredMotivos: List<MotivoMovimiento> = emptyList(),

    val isFormValid: Boolean = false,
)

class MovimientoFormManager(
    private val catalogos: Catalogos?
) {
    private val _formState = mutableStateOf(MovimientoFormState())
    val formState: State<MovimientoFormState> = _formState

    init {
        _formState.value = _formState.value.copy(
            filteredMotivos = catalogos?.motivosMovimiento ?: emptyList(),
            filteredEspecies = catalogos?.especies ?: emptyList()
        )
    }

    fun onEspecieSelected(especie: Especie) {
        val filteredCategorias = catalogos?.categorias?.filter {
            it.especieId == especie.id
        } ?: emptyList()
        val filteredRazas = catalogos?.razas?.filter {
            it.especieId == especie.id
        } ?: emptyList()

        _formState.value = _formState.value.copy(
            selectedEspecie = especie,
            filteredCategorias = filteredCategorias,
            filteredRazas = filteredRazas,
            selectedCategoria = null,
            selectedRaza = null
        )
        validateForm()
    }

    fun onCategoriaSelected(categoria: Categoria) {
        _formState.value = _formState.value.copy(selectedCategoria = categoria)
        validateForm()
    }

    fun onRazaSelected(raza: Raza) {
        _formState.value = _formState.value.copy(selectedRaza = raza)
        validateForm()
    }

    fun onMotivoSelected(motivo: MotivoMovimiento) {
        _formState.value = _formState.value.copy(selectedMotivo = motivo)
        validateForm()
    }

    fun onCantidadChanged(cantidad: String) {
        if (cantidad.all { it.isDigit() }) {
            _formState.value = _formState.value.copy(cantidad = cantidad)
            validateForm()
        }
    }

    fun onDestinoChanged(destino: String) {
        if (destino.length <= 255) {
            _formState.value = _formState.value.copy(destino = destino)
            validateForm()
        }
    }

    private fun validateForm() {
        val s = _formState.value
        val isDestinoRequired = s.selectedMotivo?.nombre?.contains("Traslado", ignoreCase = true) == true ||
                s.selectedMotivo?.nombre?.contains("Venta", ignoreCase = true) == true ||
                s.selectedMotivo?.nombre?.contains("Compra", ignoreCase = true) == true

        val isValid = s.selectedEspecie != null &&
                s.selectedCategoria != null &&
                s.selectedRaza != null &&
                s.selectedMotivo != null &&
                s.cantidad.isNotBlank() && s.cantidad.toIntOrNull() ?: 0 > 0 &&
                (!isDestinoRequired || s.destino.isNotBlank())

        _formState.value = _formState.value.copy(isFormValid = isValid)
    }
}
