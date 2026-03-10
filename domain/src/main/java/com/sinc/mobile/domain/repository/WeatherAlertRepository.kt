package com.sinc.mobile.domain.repository

import com.sinc.mobile.domain.model.WeatherAlert
import com.sinc.mobile.domain.util.Result
import com.sinc.mobile.domain.util.Error
import kotlinx.coroutines.flow.Flow

interface WeatherAlertRepository {
    fun getAlerts(): Flow<List<WeatherAlert>>
    suspend fun syncAlerts(): Result<Unit, Error>
    suspend fun markAsRead(alertId: Int)
    suspend fun markAllAsRead()
    suspend fun deleteOldAlerts()
    suspend fun saveAlert(alert: WeatherAlert)
}
