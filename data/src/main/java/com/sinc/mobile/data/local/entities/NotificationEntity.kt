package com.sinc.mobile.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val body: String,
    val receivedAt: LocalDateTime,
    val isRead: Boolean = false,
    val type: String?,
    val dataJson: String?
)
