package com.sinc.mobile.data.network.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SueloRequestDto(
    @SerialName("tipo_suelo_id") val tipoSueloId: Int,
    @SerialName("porcentaje") val porcentaje: Int
)
