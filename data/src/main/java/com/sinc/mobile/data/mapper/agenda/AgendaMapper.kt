package com.sinc.mobile.data.mapper.agenda

import com.sinc.mobile.data.local.entities.agenda.AgendaEntity
import com.sinc.mobile.data.network.dto.agenda.AgendaItemDto
import com.sinc.mobile.domain.model.agenda.AgendaItem
import com.sinc.mobile.domain.util.TimeManager
import java.time.LocalDateTime

fun AgendaItemDto.toEntity(timeManager: TimeManager): AgendaEntity {
    return AgendaEntity(
        id = id,
        userId = userId,
        titulo = titulo,
        descripcion = descripcion,
        tipo = tipo,
        fechaProgramada = timeManager.toLocalTime(fechaProgramada),
        completadaEn = completadaEn?.let { timeManager.toLocalTime(it) },
        notificado = notificado,
        sincronizado = true, // Si viene del servidor, estÃ¡ sincronizado
        createdAt = createdAt?.let { timeManager.toLocalTime(it) },
        updatedAt = updatedAt?.let { timeManager.toLocalTime(it) }
    )
}

fun AgendaEntity.toDomain(): AgendaItem {
    return AgendaItem(
        id = localId, // Usamos localId como ID de referencia para la UI
        userId = userId,
        titulo = titulo,
        descripcion = descripcion,
        tipo = tipo,
        fechaProgramada = fechaProgramada,
        completadaEn = completadaEn,
        notificado = notificado,
        isSynced = sincronizado,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun AgendaItem.toEntity(): AgendaEntity {
    return AgendaEntity(
        localId = id,
        userId = userId,
        titulo = titulo,
        descripcion = descripcion,
        tipo = tipo,
        fechaProgramada = fechaProgramada,
        completadaEn = completadaEn,
        notificado = notificado,
        sincronizado = isSynced,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun AgendaItem.toDto(timeManager: TimeManager): AgendaItemDto {
    return AgendaItemDto(
        id = 0, // El servidor asignarÃ¡ el ID real en el POST
        userId = userId,
        titulo = titulo,
        descripcion = descripcion,
        tipo = tipo,
        fechaProgramada = timeManager.toServerString(fechaProgramada),
        completadaEn = completadaEn?.let { timeManager.toServerString(it) },
        notificado = notificado,
        createdAt = createdAt?.let { timeManager.toServerString(it) },
        updatedAt = updatedAt?.let { timeManager.toServerString(it) }
    )
}
