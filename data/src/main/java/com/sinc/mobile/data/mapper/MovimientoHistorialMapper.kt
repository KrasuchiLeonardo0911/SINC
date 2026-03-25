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
    val parsedDate = try {
        LocalDateTime.parse(fechaRegistro, flexibleFormatter)
    } catch (e: Exception) {
        LocalDateTime.parse(fechaRegistro, DateTimeFormatter.ISO_DATE_TIME)
    }

    return MovimientoHistorialEntity(
        localId = 0, // Auto-generated
        id = id,
        fechaRegistro = parsedDate,
        cantidad = cantidad,
        especie = especie,
        especieId = especieId,
        categoria = categoria,
        categoriaId = categoriaId,
        raza = raza,
        razaId = razaId,
        motivo = motivo,
        motivoId = motivoId,
        tipoMovimiento = tipoMovimiento,
        unidadProductiva = unidadProductiva,
        unidadProductivaId = unidadProductivaId,
        destinoTraslado = destinoTraslado,
        sincronizado = true // If it comes from server, it's synced
    )
}

fun MovimientoHistorialEntity.toDomain(): MovimientoHistorial {
    return MovimientoHistorial(
        localId = localId,
        id = id,
        fechaRegistro = fechaRegistro,
        cantidad = cantidad,
        especie = especie,
        especieId = especieId,
        categoria = categoria,
        categoriaId = categoriaId,
        raza = raza,
        razaId = razaId,
        motivo = motivo,
        motivoId = motivoId,
        tipoMovimiento = tipoMovimiento,
        unidadProductiva = unidadProductiva,
        unidadProductivaId = unidadProductivaId,
        destinoTraslado = destinoTraslado,
        sincronizado = sincronizado
    )
}

fun MovimientoHistorial.toEntity(): MovimientoHistorialEntity {
    return MovimientoHistorialEntity(
        localId = localId,
        id = id,
        fechaRegistro = fechaRegistro,
        cantidad = cantidad,
        especie = especie,
        especieId = especieId,
        categoria = categoria,
        categoriaId = categoriaId,
        raza = raza,
        razaId = razaId,
        motivo = motivo,
        motivoId = motivoId,
        tipoMovimiento = tipoMovimiento,
        unidadProductiva = unidadProductiva,
        unidadProductivaId = unidadProductivaId,
        destinoTraslado = destinoTraslado,
        sincronizado = sincronizado
    )
}
