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
        createdAt = createdAt?.let { timeManager.toLocalTime(it) },
        updatedAt = updatedAt?.let { timeManager.toLocalTime(it) }
    )
}

fun AgendaEntity.toDomain(): AgendaItem {
    return AgendaItem(
        id = id,
        userId = userId,
        titulo = titulo,
        descripcion = descripcion,
        tipo = tipo,
        fechaProgramada = fechaProgramada,
        completadaEn = completadaEn,
        notificado = notificado,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun AgendaItem.toEntity(): AgendaEntity {
    return AgendaEntity(
        id = id,
        userId = userId,
        titulo = titulo,
        descripcion = descripcion,
        tipo = tipo,
        fechaProgramada = fechaProgramada,
        completadaEn = completadaEn,
        notificado = notificado,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun AgendaItem.toDto(timeManager: TimeManager): AgendaItemDto {
    return AgendaItemDto(
        id = id,
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
