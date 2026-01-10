package com.sinc.mobile.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UnidadProductivaStockDto(
    @SerialName("id") val id: Int,
    @SerialName("nombre") val nombre: String,
    @SerialName("stock_total") val stockTotal: Int,
    @SerialName("especies") val especies: List<EspecieStockDto>
)
