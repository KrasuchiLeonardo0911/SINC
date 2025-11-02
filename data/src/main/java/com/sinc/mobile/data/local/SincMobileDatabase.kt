package com.sinc.mobile.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.sinc.mobile.data.local.dao.CategoriaAnimalDao
import com.sinc.mobile.data.local.dao.EspecieDao
import com.sinc.mobile.data.local.dao.MotivoMovimientoDao
import com.sinc.mobile.data.local.dao.MovimientoPendienteDao
import com.sinc.mobile.data.local.dao.RazaDao
import com.sinc.mobile.data.local.dao.UnidadProductivaDao
import com.sinc.mobile.data.local.entities.CategoriaAnimalEntity
import com.sinc.mobile.data.local.entities.EspecieEntity
import com.sinc.mobile.data.local.entities.MotivoMovimientoEntity
import com.sinc.mobile.data.local.entities.MovimientoPendienteEntity
import com.sinc.mobile.data.local.entities.RazaEntity
import com.sinc.mobile.data.local.entities.UnidadProductivaEntity
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Database(
    entities = [
        UnidadProductivaEntity::class,
        EspecieEntity::class,
        RazaEntity::class,
        CategoriaAnimalEntity::class,
        MotivoMovimientoEntity::class,
        MovimientoPendienteEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class SincMobileDatabase : RoomDatabase() {
    abstract fun unidadProductivaDao(): UnidadProductivaDao
    abstract fun especieDao(): EspecieDao
    abstract fun razaDao(): RazaDao
    abstract fun categoriaAnimalDao(): CategoriaAnimalDao
    abstract fun motivoMovimientoDao(): MotivoMovimientoDao
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
