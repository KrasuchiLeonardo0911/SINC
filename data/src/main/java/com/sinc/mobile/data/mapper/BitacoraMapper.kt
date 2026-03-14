package com.sinc.mobile.data.mapper

import com.sinc.mobile.data.local.entities.BitacoraEntity
import com.sinc.mobile.data.network.dto.BitacoraDto
import com.sinc.mobile.domain.model.Bitacora
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun BitacoraDto.toEntity(): BitacoraEntity {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    
    // Si la fecha solo tiene yyyy-MM-dd, agregamos 00:00:00
    val fechaFinal = if (fecha.length == 10) "$fecha 00:00:00" else fecha
    
    return BitacoraEntity(
        id = id,
        userId = user_id,
        fecha = LocalDateTime.parse(fechaFinal, formatter),
        contenido = contenido,
        createdAt = created_at?.let { LocalDateTime.parse(it, formatter) },
        updatedAt = updated_at?.let { LocalDateTime.parse(it, formatter) }
    )
}

fun BitacoraEntity.toDomain(): Bitacora {
    return Bitacora(
        id = id,
        userId = userId,
        fecha = fecha,
        contenido = contenido,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
