package com.sinc.mobile.domain.model

import java.time.LocalDateTime

data class MovimientoHistorial(
    val localId: Long = 0,
    val id: Long? = null,
    val fechaRegistro: LocalDateTime,
    val cantidad: Int,
    val especie: String,
    val especieId: Int = 0,
    val categoria: String,
    val categoriaId: Int = 0,
    val raza: String,
    val razaId: Int = 0,
    val motivo: String,
    val motivoId: Int = 0,
    val tipoMovimiento: String,
    val unidadProductiva: String,
    val unidadProductivaId: Int = 0,
    val destinoTraslado: String?,
    val sincronizado: Boolean = false
)
