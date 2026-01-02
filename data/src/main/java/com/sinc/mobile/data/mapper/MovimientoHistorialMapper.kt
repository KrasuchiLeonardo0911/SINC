package com.sinc.mobile.data.mapper

import com.sinc.mobile.data.local.entities.MovimientoHistorialEntity
import com.sinc.mobile.data.model.MovimientoHistorialDto
import com.sinc.mobile.domain.model.MovimientoHistorial
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun MovimientoHistorialDto.toEntity(): MovimientoHistorialEntity {
    return MovimientoHistorialEntity(
        id = id,
        fechaRegistro = LocalDateTime.parse(fechaRegistro, DateTimeFormatter.ISO_DATE_TIME),
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
