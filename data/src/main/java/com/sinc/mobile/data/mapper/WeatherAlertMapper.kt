package com.sinc.mobile.data.mapper

import android.util.Log
import com.sinc.mobile.data.local.entities.WeatherAlertEntity
import com.sinc.mobile.data.network.dto.WeatherAlertDto
import com.sinc.mobile.domain.model.WeatherAlert
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun WeatherAlertDto.toEntity(): WeatherAlertEntity {
    return try {
        WeatherAlertEntity(
            upId = upId,
            upNombre = upNombre,
            municipio = municipio,
            evento = evento,
            nivel = nivel,
            inicio = LocalDateTime.parse(inicio, DateTimeFormatter.ISO_OFFSET_DATE_TIME),
            fin = LocalDateTime.parse(fin, DateTimeFormatter.ISO_OFFSET_DATE_TIME),
            descripcion = descripcion
        )
    } catch (e: Exception) {
        Log.e("WeatherMapper", "Error parseando fechas: inicio='$inicio', fin='$fin'", e)
        // Fallback a fecha actual para evitar crash, pero el log nos dirá el problema
        WeatherAlertEntity(
            upId = upId,
            upNombre = upNombre,
            municipio = municipio,
            evento = evento,
            nivel = nivel,
            inicio = LocalDateTime.now(),
            fin = LocalDateTime.now().plusDays(1),
            descripcion = descripcion
        )
    }
}

fun WeatherAlertEntity.toDomain(): WeatherAlert {
    return WeatherAlert(
        id = id,
        upId = upId,
        upNombre = upNombre,
        municipio = municipio,
        evento = evento,
        nivel = nivel,
        inicio = inicio,
        fin = fin,
        descripcion = descripcion,
        isRead = isRead
    )
}

fun WeatherAlert.toEntity(): WeatherAlertEntity {
    return WeatherAlertEntity(
        id = id,
        upId = upId,
        upNombre = upNombre,
        municipio = municipio,
        evento = evento,
        nivel = nivel,
        inicio = inicio,
        fin = fin,
        descripcion = descripcion,
        isRead = isRead
    )
}
