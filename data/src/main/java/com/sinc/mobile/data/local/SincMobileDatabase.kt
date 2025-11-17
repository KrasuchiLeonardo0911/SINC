package com.sinc.mobile.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.sinc.mobile.data.local.dao.CatalogosDao
import com.sinc.mobile.data.local.dao.MovimientoPendienteDao
import com.sinc.mobile.data.local.dao.UnidadProductivaDao
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
        TipoPastoEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class SincMobileDatabase : RoomDatabase() {
    abstract fun unidadProductivaDao(): UnidadProductivaDao
    abstract fun catalogosDao(): CatalogosDao
    abstract fun movimientoPendienteDao(): MovimientoPendienteDao
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
}
