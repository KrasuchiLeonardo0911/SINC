package com.sinc.mobile.app.features.cuaderno

import com.sinc.mobile.domain.model.Bitacora
import java.time.LocalDate

enum class CuadernoView {
    LISTADO,
    CATEGORIAS,
    PASO_FECHA,
    PASO_DESCRIPCION
}

data class BitacoraState(
    val bitacoras: List<Bitacora> = emptyList(),
    val filteredBitacoras: List<Bitacora> = emptyList(),
    val currentView: CuadernoView = CuadernoView.LISTADO,
    
    // Fecha seleccionada para el filtro del listado (mes/año)
    val selectedDate: LocalDate = LocalDate.now(),
    
    // Datos temporales para el nuevo registro
    val tempDate: LocalDate = LocalDate.now(),
    val tempContent: String = "",
    
    val isLoading: Boolean = false,
    val isInitialLoad: Boolean = true,
    val error: String? = null,
    val isSaving: Boolean = false
)
