package com.sinc.mobile.data.network.dto

import com.google.gson.annotations.SerializedName

data class MovimientoRequest(
    @SerializedName("especie_id") val especie_id: Int,
    @SerializedName("categoria_id") val categoria_id: Int,
    @SerializedName("raza_id") val raza_id: Int,
    @SerializedName("cantidad") val cantidad: Int,
    @SerializedName("motivo_movimiento_id") val motivo_movimiento_id: Int,
    @SerializedName("destino_traslado") val destino_traslado: String?,
)