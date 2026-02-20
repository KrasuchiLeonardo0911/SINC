package com.sinc.mobile.data.mapper

import com.sinc.mobile.data.local.entities.MovimientoHistorialEntity
import com.sinc.mobile.data.model.MovimientoHistorialDto
import com.sinc.mobile.domain.model.MovimientoHistorial
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField

private val flexibleFormatter = DateTimeFormatterBuilder()
    .appendPattern("[yyyy-MM-dd'T'HH:mm:ss]")
    .appendPattern("[yyyy-MM-dd HH:mm:ss]")
    .appendPattern("[yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z']")
    .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
    .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
    .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
    .toFormatter()

fun MovimientoHistorialDto.toEntity(): MovimientoHistorialEntity {
    // Clean potential millisecond/timezone suffix if simple parsing fails, 
    // or rely on the flexible formatter
    val cleanDate = fechaRegistro.split(".").first() // Simplistic approach to remove .000000Z if causing issues with simple patterns, but let's try formatter first.
    
    // Better approach: Try/Catch fallback or flexible pattern
    val parsedDate = try {
        LocalDateTime.parse(fechaRegistro, flexibleFormatter)
    } catch (e: Exception) {
        // Fallback for standard ISO if flexible fails
        LocalDateTime.parse(fechaRegistro, DateTimeFormatter.ISO_DATE_TIME)
    }

    return MovimientoHistorialEntity(
        id = id,
        fechaRegistro = parsedDate,
        cantidad = cantidad,
        especie = especie,
        categoria = categoria,
        raza = raza,
        motivo = motivo,
        tipoMovimiento = tipoMovimiento,
        unidadProductiva = unidadProductiva,
        destinoTraslado = destinoTraslado
    )
}

fun MovimientoHistorialEntity.toDomain(): MovimientoHistorial {
    return MovimientoHistorial(
        id = id,
        fechaRegistro = fechaRegistro,
        cantidad = cantidad,
        especie = especie,
        categoria = categoria,
        raza = raza,
        motivo = motivo,
        tipoMovimiento = tipoMovimiento,
        unidadProductiva = unidadProductiva,
        destinoTraslado = destinoTraslado
    )
}
