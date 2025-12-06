package com.sinc.mobile.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.sinc.mobile.data.local.dao.CategoriaAnimalDao
import com.sinc.mobile.data.local.dao.CondicionTenenciaDao
import com.sinc.mobile.data.local.dao.EspecieDao
import com.sinc.mobile.data.local.dao.FuenteAguaDao
import com.sinc.mobile.data.local.dao.MotivoMovimientoDao
import com.sinc.mobile.data.local.dao.MovimientoPendienteDao
import com.sinc.mobile.data.local.dao.MunicipioDao
import com.sinc.mobile.data.local.dao.RazaDao
import com.sinc.mobile.data.local.dao.TipoPastoDao
import com.sinc.mobile.data.local.dao.TipoSueloDao
import com.sinc.mobile.data.local.dao.UnidadProductivaDao
import com.sinc.mobile.data.local.entity.IdentifierConfigEntity
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
        IdentifierConfigEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class SincMobileDatabase : RoomDatabase() {
    abstract fun unidadProductivaDao(): UnidadProductivaDao
    abstract fun movimientoPendienteDao(): MovimientoPendienteDao

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
    abstract fun identifierConfigDao(): com.sinc.mobile.data.local.dao.IdentifierConfigDao
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
