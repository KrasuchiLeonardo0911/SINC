package com.sinc.mobile.domain.use_case.weather

import com.sinc.mobile.domain.repository.WeatherAlertRepository
import com.sinc.mobile.domain.util.Result
import com.sinc.mobile.domain.util.Error
import javax.inject.Inject

class SyncWeatherAlertsUseCase @Inject constructor(
    private val repository: WeatherAlertRepository
) {
    suspend operator fun invoke(): Result<Unit, Error> = repository.syncAlerts()
}
