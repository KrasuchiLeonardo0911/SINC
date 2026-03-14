package com.sinc.mobile.data.util

import android.content.SharedPreferences
import android.util.Log
import com.sinc.mobile.domain.util.TimeManager
import java.time.*
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimeManagerImpl @Inject constructor(
    private val prefs: SharedPreferences
) : TimeManager {

    private val PREF_TIME_OFFSET = "server_time_offset_ms"
    private val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME

    // Se inicializa cargando el último offset guardado (si existe)
    private var timeOffsetMs: Long = prefs.getLong(PREF_TIME_OFFSET, 0L)

    override fun updateServerTime(serverTimeIso: String) {
        try {
            val serverTime = OffsetDateTime.parse(serverTimeIso).toInstant()
            val deviceTime = Instant.now()
            
            timeOffsetMs = Duration.between(deviceTime, serverTime).toMillis()
            
            prefs.edit().putLong(PREF_TIME_OFFSET, timeOffsetMs).apply()
            
            Log.d("TimeManager", "Sincronizado con servidor. Offset: ${timeOffsetMs}ms. Hora Servidor: ${nowAtServer()}")
        } catch (e: Exception) {
            Log.e("TimeManager", "Error sincronizando tiempo: $serverTimeIso", e)
        }
    }

    override fun nowAtServer(): LocalDateTime {
        return LocalDateTime.now(Clock.offset(Clock.systemDefaultZone(), Duration.ofMillis(timeOffsetMs)))
    }

    override fun toServerString(localDateTime: LocalDateTime): String {
        return localDateTime
            .atZone(ZoneId.systemDefault())
            .toOffsetDateTime()
            .format(formatter)
    }

    override fun toLocalTime(serverDateTimeIso: String): LocalDateTime {
        return try {
            // El servidor envía ISO 8601 (ej. 2026-03-13T10:00:00-03:00)
            OffsetDateTime.parse(serverDateTimeIso.replace(" ", "T"))
                .atZoneSameInstant(ZoneId.systemDefault())
                .toLocalDateTime()
        } catch (e: Exception) {
            try {
                // Fallback: tratar como LocalDateTime "pelado" (asumiendo que ya es local)
                LocalDateTime.parse(serverDateTimeIso.replace(" ", "T").take(19))
            } catch (e2: Exception) {
                // Último recurso: devolver ahora si falla todo
                nowAtServer()
            }
        }
    }
}
