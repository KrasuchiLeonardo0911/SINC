package com.sinc.mobile.domain.use_case.notification

import com.sinc.mobile.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUnreadNotificationCountUseCase @Inject constructor(
    private val repository: NotificationRepository
) {
    operator fun invoke(): Flow<Int> {
        return repository.getUnreadNotificationCount()
    }
}
