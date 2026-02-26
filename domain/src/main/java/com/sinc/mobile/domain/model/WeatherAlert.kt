package com.sinc.mobile.domain.model

import java.time.LocalDateTime

data class WeatherAlert(
    val id: Int = 0, // ID local (autoincrement)
    val upId: Int,
    val upNombre: String,
    val municipio: String,
    val evento: String,
    val nivel: String, // rojo, naranja, amarillo
    val inicio: LocalDateTime,
    val fin: LocalDateTime,
    val descripcion: String,
    val isRead: Boolean = false
)
