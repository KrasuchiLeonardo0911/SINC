package com.sinc.mobile.data.mapper

import com.sinc.mobile.data.local.entities.NotificationEntity
import com.sinc.mobile.domain.model.Notification
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

fun NotificationEntity.toDomain(): Notification {
    val dataMap = dataJson?.let {
        try {
            Json.decodeFromString(MapSerializer(String.serializer(), String.serializer()), it)
        } catch (e: Exception) {
            // Log error or handle gracefully
            null
        }
    }

    return Notification(
        id = id,
        title = title,
        body = body,
        receivedAt = receivedAt,
        isRead = isRead,
        type = type,
        data = dataMap
    )
}
