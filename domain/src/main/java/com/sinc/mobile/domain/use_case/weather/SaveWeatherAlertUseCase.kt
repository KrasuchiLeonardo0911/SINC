package com.sinc.mobile.domain.use_case.weather

import com.sinc.mobile.domain.model.WeatherAlert
import com.sinc.mobile.domain.repository.WeatherAlertRepository
import javax.inject.Inject

class SaveWeatherAlertUseCase @Inject constructor(
    private val repository: WeatherAlertRepository
) {
    suspend operator fun invoke(alert: WeatherAlert) = repository.saveAlert(alert)
}
