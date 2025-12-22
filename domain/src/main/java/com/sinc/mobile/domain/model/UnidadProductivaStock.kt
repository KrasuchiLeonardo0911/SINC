package com.sinc.mobile.domain.model

data class UnidadProductivaStock(
    val id: Int,
    val nombre: String,
    val stockTotal: Int,
    val especies: List<EspecieStock>
)
