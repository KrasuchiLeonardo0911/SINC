package com.sinc.mobile.data.model

import com.google.gson.annotations.SerializedName

data class EspecieStockDto(
    @SerializedName("nombre") val nombre: String,
    @SerializedName("stock_total") val stockTotal: Int,
    @SerializedName("desglose") val desglose: List<DesgloseStockDto>
)
