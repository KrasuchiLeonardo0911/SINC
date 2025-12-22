package com.sinc.mobile.data.model

import com.google.gson.annotations.SerializedName

data class UnidadProductivaStockDto(
    @SerializedName("id") val id: Int,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("stock_total") val stockTotal: Int,
    @SerializedName("especies") val especies: List<EspecieStockDto>
)
