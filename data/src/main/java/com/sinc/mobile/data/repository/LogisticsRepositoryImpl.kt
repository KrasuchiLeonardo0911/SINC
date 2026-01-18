package com.sinc.mobile.data.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.sinc.mobile.data.network.api.LogisticsApiService
import com.sinc.mobile.domain.model.LogisticsInfo
import com.sinc.mobile.domain.repository.LogisticsRepository
import com.sinc.mobile.domain.util.Result
import com.sinc.mobile.domain.util.Error
import com.sinc.mobile.domain.model.GenericError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

import java.time.ZonedDateTime

class LogisticsRepositoryImpl @Inject constructor(
    private val apiService: LogisticsApiService
) : LogisticsRepository {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun getLogisticsInfo(): Flow<Result<LogisticsInfo, Error>> = flow {
        try {
            val response = apiService.getLogisticsInfo()
            if (response.isSuccessful && response.body() != null) {
                val dto = response.body()!!
                val proximaVisita = dto.proximaVisita?.let {
                    try {
                        // Parse ISO Offset Date Time (e.g., 2026-01-23T00:00:00+00:00)
                        ZonedDateTime.parse(it, DateTimeFormatter.ISO_OFFSET_DATE_TIME).toLocalDate()
                    } catch (e: Exception) {
                        try {
                            // Fallback to ISO Local Date
                            LocalDate.parse(it, DateTimeFormatter.ISO_LOCAL_DATE)
                        } catch (e2: Exception) {
                            null
                        }
                    }
                }
                
                emit(Result.Success(LogisticsInfo(
                    proximaVisita = proximaVisita,
                    frecuenciaDias = dto.frecuenciaDias ?: 0
                )))
            } else {
                emit(Result.Failure(GenericError("Error al obtener información de logística")))
            }
        } catch (e: Exception) {
            emit(Result.Failure(GenericError(e.message ?: "Error desconocido")))
        }
    }
}
