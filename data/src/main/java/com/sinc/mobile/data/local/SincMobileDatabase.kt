package com.sinc.mobile.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.sinc.mobile.data.local.dao.*
import com.sinc.mobile.data.local.entities.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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
        MessageEntity::class
    ],
    version = 7,
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
    @androidx.room.TypeConverter
    fun fromTimestamp(value: String?): LocalDateTime? {
        return value?.let { LocalDateTime.parse(it, DateTimeFormatter.ISO_LOCAL_DATE_TIME) }
    }

    @androidx.room.TypeConverter
    fun dateToTimestamp(date: LocalDateTime?): String? {
        return date?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    }
}
