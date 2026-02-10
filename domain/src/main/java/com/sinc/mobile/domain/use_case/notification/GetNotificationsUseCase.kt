package com.sinc.mobile.domain.use_case.notification

import com.sinc.mobile.domain.model.Notification
import com.sinc.mobile.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetNotificationsUseCase @Inject constructor(
    private val repository: NotificationRepository
) {
    operator fun invoke(): Flow<List<Notification>> {
        return repository.getNotifications()
    }
}
