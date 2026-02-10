package com.sinc.mobile.domain.use_case.notification

import com.sinc.mobile.domain.repository.NotificationRepository
import javax.inject.Inject

class DeleteNotificationUseCase @Inject constructor(
    private val repository: NotificationRepository
) {
    suspend operator fun invoke(notificationId: Long) {
        repository.deleteNotification(notificationId)
    }
}
