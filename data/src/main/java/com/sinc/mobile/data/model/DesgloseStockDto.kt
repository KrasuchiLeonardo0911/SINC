package com.sinc.mobile.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DesgloseStockDto(
    @SerialName("categoria") val categoria: String,
    @SerialName("raza") val raza: String,
    @SerialName("cantidad") val cantidad: Int
)
