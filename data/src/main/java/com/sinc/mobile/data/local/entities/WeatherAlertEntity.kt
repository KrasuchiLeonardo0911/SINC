package com.sinc.mobile.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "weather_alerts")
data class WeatherAlertEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val upId: Int,
    val upNombre: String,
    val municipio: String,
    val evento: String,
    val nivel: String,
    val inicio: LocalDateTime,
    val fin: LocalDateTime,
    val descripcion: String,
    val isRead: Boolean = false,
    val createdAt: LocalDateTime = LocalDateTime.now()
)
