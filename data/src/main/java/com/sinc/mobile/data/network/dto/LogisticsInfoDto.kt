package com.sinc.mobile.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LogisticsInfoDto(
    @SerialName("fecha") val proximaVisita: String?, // Format: ISO 8601 with Offset
    @SerialName("frecuencia_dias") val frecuenciaDias: Int?
)
