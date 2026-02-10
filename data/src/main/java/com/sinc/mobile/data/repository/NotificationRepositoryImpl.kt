package com.sinc.mobile.data.repository

import com.sinc.mobile.data.local.dao.NotificationDao
import com.sinc.mobile.data.local.entities.NotificationEntity
import com.sinc.mobile.data.mapper.toDomain
import com.sinc.mobile.domain.model.Notification
import com.sinc.mobile.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val notificationDao: NotificationDao
) : NotificationRepository {

    override fun getNotifications(): Flow<List<Notification>> {
        return notificationDao.getAllNotifications().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun saveNotification(title: String, body: String, type: String?, data: Map<String, String>?) {
        val dataJson = data?.let { Json.encodeToString(it) }
        val notificationEntity = NotificationEntity(
            title = title,
            body = body,
            receivedAt = LocalDateTime.now(),
            type = type,
            dataJson = dataJson
        )
        notificationDao.insertNotification(notificationEntity)
    }

    override fun getUnreadNotificationCount(): Flow<Int> {
        return notificationDao.getUnreadNotificationCount()
    }

    override suspend fun markNotificationAsRead(notificationId: Long) {
        notificationDao.markAsRead(notificationId)
    }

    override suspend fun deleteNotification(notificationId: Long) {
        notificationDao.deleteNotificationById(notificationId)
    }
}
