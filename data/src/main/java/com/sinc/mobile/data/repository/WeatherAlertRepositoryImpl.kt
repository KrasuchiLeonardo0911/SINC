package com.sinc.mobile.data.repository

import android.util.Log
import com.sinc.mobile.data.local.dao.WeatherAlertDao
import com.sinc.mobile.data.mapper.toDomain
import com.sinc.mobile.data.mapper.toEntity
import com.sinc.mobile.data.network.api.WeatherAlertApiService
import com.sinc.mobile.domain.model.WeatherAlert
import com.sinc.mobile.domain.repository.WeatherAlertRepository
import com.sinc.mobile.domain.util.Error
import com.sinc.mobile.domain.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class WeatherAlertRepositoryImpl @Inject constructor(
    private val apiService: WeatherAlertApiService,
    private val dao: WeatherAlertDao
) : WeatherAlertRepository {

    override fun getAlerts(): Flow<List<WeatherAlert>> {
        return dao.getAllAlerts().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun syncAlerts(): Result<Unit, Error> {
        Log.d("WeatherSync", "Iniciando sincronización de alertas...")
        return try {
            val response = apiService.getActiveAlerts()
            if (response.isSuccessful && response.body() != null) {
                val alertsDto = response.body()!!.alertas
                Log.d("WeatherSync", "Alertas recibidas: ${alertsDto.size}")
                alertsDto.forEach { Log.d("WeatherSync", "Alerta DTO: $it") }
                
                dao.deleteAll()
                val entities = alertsDto.map { it.toEntity() }
                dao.insertAlerts(entities)
                Log.d("WeatherSync", "Sincronización exitosa")
                Result.Success(Unit)
            } else {
                Log.e("WeatherSync", "Error en API: ${response.code()}")
                Result.Failure(object : Error {
                    override val message: String = "Error al sincronizar alertas: ${response.code()}"
                })
            }
        } catch (e: Exception) {
            Log.e("WeatherSync", "Excepción en syncAlerts", e)
            Result.Failure(object : Error {
                override val message: String = e.localizedMessage ?: "Error desconocido"
            })
        }
    }

    override suspend fun markAsRead(alertId: Int) {
        dao.markAsRead(alertId)
    }

    override suspend fun deleteOldAlerts() {
        dao.deleteOldAlerts(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
    }

    override suspend fun saveAlert(alert: WeatherAlert) {
        dao.insertAlert(alert.toEntity())
    }
}
