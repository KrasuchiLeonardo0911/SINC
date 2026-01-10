package com.sinc.mobile.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EspecieStockDto(
    @SerialName("nombre") val nombre: String,
    @SerialName("stock_total") val stockTotal: Int,
    @SerialName("desglose") val desglose: List<DesgloseStockDto>
)
