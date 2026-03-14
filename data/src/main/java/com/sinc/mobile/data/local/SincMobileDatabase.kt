package com.sinc.mobile.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.sinc.mobile.data.local.dao.*
import com.sinc.mobile.data.local.entities.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer


@Database(
    entities = [
        UnidadProductivaEntity::class,
        EspecieEntity::class,
        RazaEntity::class,
        CategoriaAnimalEntity::class,
        MotivoMovimientoEntity::class,
        MovimientoPendienteEntity::class,
        MunicipioEntity::class,
        CondicionTenenciaEntity::class,
        FuenteAguaEntity::class,
        TipoSueloEntity::class,
        TipoPastoEntity::class,
        IdentifierConfigEntity::class,
        StockEntity::class,
        MovimientoHistorialEntity::class,
        DeclaracionVentaEntity::class,
        TicketEntity::class,
        MessageEntity::class,
        NotificationEntity::class,
        UnidadProductivaTipoSueloCrossRef::class,
        UnidadProductivaTipoPastoCrossRef::class,
        WeatherAlertEntity::class,
        com.sinc.mobile.data.local.entities.agenda.AgendaEntity::class,
        BitacoraEntity::class
    ],
    version = 19,
    exportSchema = false
)
@TypeConverters(Converters::class, StockTypeConverter::class)
abstract class SincMobileDatabase : RoomDatabase() {
    abstract fun unidadProductivaDao(): UnidadProductivaDao
    abstract fun movimientoPendienteDao(): MovimientoPendienteDao
    abstract fun stockDao(): StockDao
    abstract fun movimientoHistorialDao(): MovimientoHistorialDao
    abstract fun declaracionVentaDao(): DeclaracionVentaDao
    abstract fun ticketDao(): TicketDao
    abstract fun notificationDao(): NotificationDao
    abstract fun weatherAlertDao(): WeatherAlertDao
    abstract fun agendaDao(): com.sinc.mobile.data.local.dao.AgendaDao
    abstract fun bitacoraDao(): BitacoraDao

    // DAOs de Catálogos
    abstract fun especieDao(): EspecieDao
    abstract fun razaDao(): RazaDao
    abstract fun categoriaAnimalDao(): CategoriaAnimalDao
    abstract fun motivoMovimientoDao(): MotivoMovimientoDao
    abstract fun municipioDao(): MunicipioDao
    abstract fun condicionTenenciaDao(): CondicionTenenciaDao
    abstract fun fuenteAguaDao(): FuenteAguaDao
    abstract fun tipoSueloDao(): TipoSueloDao
    abstract fun tipoPastoDao(): TipoPastoDao
    abstract fun identifierConfigDao(): IdentifierConfigDao
}

class Converters {
    @TypeConverter
    fun fromTimestamp(value: String?): LocalDateTime? {
        return value?.let { LocalDateTime.parse(it, DateTimeFormatter.ISO_LOCAL_DATE_TIME) }
    }

    @TypeConverter
    fun dateToTimestamp(date: LocalDateTime?): String? {
        return date?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    }

    @TypeConverter
    fun fromString(value: String?): Map<String, String>? {
        return value?.let { Json.decodeFromString(MapSerializer(String.serializer(), String.serializer()), it) }
    }

    @TypeConverter
    fun fromMap(map: Map<String, String>?): String? {
        return map?.let { Json.encodeToString(MapSerializer(String.serializer(), String.serializer()), it) }
    }
}
