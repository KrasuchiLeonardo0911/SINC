package com.sinc.mobile.domain.use_case.weather

import com.sinc.mobile.domain.repository.WeatherAlertRepository
import javax.inject.Inject

class MarkWeatherAlertsAsReadUseCase @Inject constructor(
    private val repository: WeatherAlertRepository
) {
    suspend operator fun invoke() {
        repository.markAllAsRead()
    }
}
