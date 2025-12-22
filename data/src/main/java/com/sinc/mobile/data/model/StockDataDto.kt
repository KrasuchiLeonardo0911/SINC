package com.sinc.mobile.data.model

import com.google.gson.annotations.SerializedName

data class StockDataDto(
    @SerializedName("unidades_productivas") val unidadesProductivas: List<UnidadProductivaStockDto>,
    @SerializedName("stock_total_general") val stockTotalGeneral: Int
)
