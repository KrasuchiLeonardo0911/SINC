package com.sinc.mobile.data.network.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PastoRequestDto(
    @SerialName("tipo_pasto_id") val tipoPastoId: Int,
    @SerialName("porcentaje") val porcentaje: Int?
)
