package com.sinc.mobile.app.features.notifications

import com.sinc.mobile.domain.model.Notification

data class NotificationsState(
    val isLoading: Boolean = true,
    val notifications: List<Notification> = emptyList(),
    val isSelectionMode: Boolean = false,
    val selectedIds: Set<Long> = emptySet()
)