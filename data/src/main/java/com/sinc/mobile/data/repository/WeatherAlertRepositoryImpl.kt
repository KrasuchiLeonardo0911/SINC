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
import kotlinx.coroutines.flow.first
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
        Log.d("WeatherSync", "Iniciando sincronización inteligente de alertas...")
        return try {
            val response = apiService.getActiveAlerts()
            if (response.isSuccessful && response.body() != null) {
                val remoteAlertsDto = response.body()!!.alertas
                
                // Obtener alertas actuales para preservar el estado 'isRead'
                val localAlerts = dao.getAllAlerts().first()
                
                val entitiesToInsert = remoteAlertsDto.map { dto ->
                    val entity = dto.toEntity()
                    // Si la alerta ya existía (mismo evento, municipio y UP), preservamos su estado isRead
                    val existing = localAlerts.find { 
                        it.evento == entity.evento && 
                        it.municipio == entity.municipio && 
                        it.upId == entity.upId 
                    }
                    if (existing != null) {
                        entity.copy(id = existing.id, isRead = existing.isRead)
                    } else {
                        entity
                    }
                }

                // Limpiar y reinsertar con estados preservados
                dao.deleteAll()
                dao.insertAlerts(entitiesToInsert)
                
                Result.Success(Unit)
            } else {
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

    override suspend fun markAllAsRead() {
        dao.markAllAsRead()
    }

    override suspend fun deleteOldAlerts() {
        dao.deleteOldAlerts(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
    }

    override suspend fun saveAlert(alert: WeatherAlert) {
        dao.insertAlert(alert.toEntity())
    }
}
