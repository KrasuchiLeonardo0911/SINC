package com.sinc.mobile.domain.model

data class EspecieStock(
    val nombre: String,
    val stockTotal: Int,
    val desglose: List<DesgloseStock>
)
