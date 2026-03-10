package com.sinc.mobile.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sinc.mobile.data.local.entities.WeatherAlertEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherAlertDao {
    @Query("SELECT * FROM weather_alerts ORDER BY inicio DESC")
    fun getAllAlerts(): Flow<List<WeatherAlertEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: WeatherAlertEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlerts(alerts: List<WeatherAlertEntity>)

    @Query("UPDATE weather_alerts SET isRead = 1 WHERE id = :alertId")
    suspend fun markAsRead(alertId: Int)

    @Query("UPDATE weather_alerts SET isRead = 1")
    suspend fun markAllAsRead()

    @Query("DELETE FROM weather_alerts WHERE fin < :now")
    suspend fun deleteOldAlerts(now: String)

    @Query("DELETE FROM weather_alerts")
    suspend fun deleteAll()
}
