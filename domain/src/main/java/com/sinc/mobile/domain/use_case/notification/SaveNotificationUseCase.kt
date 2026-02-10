package com.sinc.mobile.domain.use_case.notification

import com.sinc.mobile.domain.repository.NotificationRepository
import javax.inject.Inject

class SaveNotificationUseCase @Inject constructor(
    private val repository: NotificationRepository
) {
    suspend operator fun invoke(title: String, body: String, type: String?, data: Map<String, String>?) {
        if (title.isNotBlank() && body.isNotBlank()) {
            repository.saveNotification(title, body, type, data)
        }
    }
}
