package com.sinc.mobile.domain.repository

import com.sinc.mobile.domain.model.Notification
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    fun getNotifications(): Flow<List<Notification>>
    suspend fun saveNotification(title: String, body: String, type: String?, data: Map<String, String>?)
    fun getUnreadNotificationCount(): Flow<Int>
    suspend fun markNotificationAsRead(notificationId: Long)
    suspend fun deleteNotification(notificationId: Long)
}
