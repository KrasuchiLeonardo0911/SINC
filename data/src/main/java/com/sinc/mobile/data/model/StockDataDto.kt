package com.sinc.mobile.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StockDataDto(
    @SerialName("unidades_productivas") val unidadesProductivas: List<UnidadProductivaStockDto>,
    @SerialName("stock_total_general") val stockTotalGeneral: Int
)
