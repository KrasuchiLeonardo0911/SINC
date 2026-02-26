package com.sinc.mobile.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WeatherAlertDto(
    @SerialName("up_id") val upId: Int,
    @SerialName("up_nombre") val upNombre: String,
    val municipio: String,
    val evento: String,
    val nivel: String,
    val inicio: String,
    val fin: String,
    val descripcion: String
)

@Serializable
data class WeatherAlertResponseDto(
    val success: Boolean,
    val alertas: List<WeatherAlertDto>,
    val count: Int
)
