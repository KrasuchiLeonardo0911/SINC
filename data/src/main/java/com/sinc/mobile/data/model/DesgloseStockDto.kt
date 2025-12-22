package com.sinc.mobile.data.model

import com.google.gson.annotations.SerializedName

data class DesgloseStockDto(
    @SerializedName("categoria") val categoria: String,
    @SerializedName("raza") val raza: String,
    @SerializedName("cantidad") val cantidad: Int
)
