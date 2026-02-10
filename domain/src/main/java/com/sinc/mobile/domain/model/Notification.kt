package com.sinc.mobile.domain.model

import java.time.LocalDateTime

data class Notification(
    val id: Long,
    val title: String,
    val body: String,
    val receivedAt: LocalDateTime,
    val isRead: Boolean,
    val type: String?,
    val data: Map<String, String>?
)
