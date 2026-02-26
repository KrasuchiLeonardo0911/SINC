package com.sinc.mobile.domain.use_case.weather

import com.sinc.mobile.domain.model.WeatherAlert
import com.sinc.mobile.domain.repository.WeatherAlertRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetWeatherAlertsUseCase @Inject constructor(
    private val repository: WeatherAlertRepository
) {
    operator fun invoke(): Flow<List<WeatherAlert>> = repository.getAlerts()
}
