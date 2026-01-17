package com.sinc.mobile.domain.model

data class DeclaracionVenta(
    val id: Int,
    val productorId: Int,
    val unidadProductivaId: Int,
    val especieId: Int,
    val razaId: Int,
    val categoriaAnimalId: Int,
    val cantidad: Int,
    val estado: String,
    val fechaDeclaracion: String,
    val observaciones: String?
)
