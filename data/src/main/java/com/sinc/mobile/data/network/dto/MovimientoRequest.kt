package com.sinc.mobile.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MovimientoRequest(
    @SerialName("especie_id") val especie_id: Int,
    @SerialName("categoria_id") val categoria_id: Int,
    @SerialName("raza_id") val raza_id: Int,
    @SerialName("cantidad") val cantidad: Int,
    @SerialName("motivo_movimiento_id") val motivo_movimiento_id: Int,
    @SerialName("destino_traslado") val destino_traslado: String?,
)