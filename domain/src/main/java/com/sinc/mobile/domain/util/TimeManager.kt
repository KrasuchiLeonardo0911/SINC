package com.sinc.mobile.domain.util

import java.time.LocalDateTime

interface TimeManager {
    /**
     * Actualiza el offset de tiempo comparando la hora del servidor con la del dispositivo.
     * @param serverTimeIso Hora del servidor en formato ISO 8601 (con offset).
     */
    fun updateServerTime(serverTimeIso: String)

    /**
     * Devuelve la hora actual "sincronizada" con el servidor.
     */
    fun nowAtServer(): LocalDateTime

    /**
     * Convierte una hora local del dispositivo a una string ISO lista para el servidor (con offset).
     */
    fun toServerString(localDateTime: LocalDateTime): String

    /**
     * Convierte una string de fecha del servidor a la hora local del dispositivo.
     */
    fun toLocalTime(serverDateTimeIso: String): LocalDateTime
}
