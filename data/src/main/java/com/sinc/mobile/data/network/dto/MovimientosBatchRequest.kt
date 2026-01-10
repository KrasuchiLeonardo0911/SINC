package com.sinc.mobile.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MovimientosBatchRequest(
    @SerialName("upId") val upId: Int,
    @SerialName("movimientos") val movimientos: List<MovimientoRequest>
)
