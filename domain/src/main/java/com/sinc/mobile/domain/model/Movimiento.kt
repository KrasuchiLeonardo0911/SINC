package com.sinc.mobile.domain.model

data class Movimiento(
    val especieId: Int,
    val categoriaId: Int,
    val razaId: Int,
    val cantidad: Int,
    val motivoMovimientoId: Int,
    val destinoTraslado: String? = null
)
