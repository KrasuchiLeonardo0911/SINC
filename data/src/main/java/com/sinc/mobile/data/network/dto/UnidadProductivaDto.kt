package com.sinc.mobile.data.network.dto

import com.google.gson.annotations.SerializedName

data class UnidadProductivaDto(
    @SerializedName("id") val id: Int,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("latitud") val latitud: String?,
    @SerializedName("longitud") val longitud: String?
)
