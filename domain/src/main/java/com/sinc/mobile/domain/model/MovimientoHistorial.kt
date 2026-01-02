package com.sinc.mobile.domain.model

import java.time.LocalDateTime

data class MovimientoHistorial(
    val id: Long,
    val fechaRegistro: LocalDateTime,
    val cantidad: Int,
    val especie: String,
    val categoria: String,
    val raza: String,
    val motivo: String,
    val tipoMovimiento: String,
    val unidadProductiva: String,
    val destinoTraslado: String?
)
