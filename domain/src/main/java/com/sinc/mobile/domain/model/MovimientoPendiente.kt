package com.sinc.mobile.domain.model

import java.time.LocalDateTime

data class MovimientoPendiente(
    val id: Long = 0, // Local ID, useful for updates
    val unidadProductivaId: Int,
    val especieId: Int,
    val categoriaId: Int,
    val razaId: Int,
    val cantidad: Int,
    val motivoMovimientoId: Int,
    val destinoTraslado: String? = null,
    val observaciones: String? = null,
    val fechaRegistro: LocalDateTime, // Corresponds to backend's fecha_registro
    val sincronizado: Boolean = false // Local state
)
