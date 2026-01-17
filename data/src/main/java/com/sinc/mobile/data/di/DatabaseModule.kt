package com.sinc.mobile.data.di

import android.content.Context
import androidx.room.Room
import com.sinc.mobile.data.local.SincMobileDatabase
import com.sinc.mobile.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Singleton
    @Provides
    fun provideDatabase(
        @ApplicationContext context: Context
    ): SincMobileDatabase {
        return Room.databaseBuilder(
            context,
            SincMobileDatabase::class.java,
            "sinc_mobile_database"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    @Singleton
    @Provides
    fun provideUnidadProductivaDao(database: SincMobileDatabase): UnidadProductivaDao {
        return database.unidadProductivaDao()
    }

    @Singleton
    @Provides
    fun provideMovimientoPendienteDao(database: SincMobileDatabase): MovimientoPendienteDao {
        return database.movimientoPendienteDao()
    }

    @Singleton
    @Provides
    fun provideStockDao(database: SincMobileDatabase): StockDao {
        return database.stockDao()
    }

    @Singleton
    @Provides
    fun provideMovimientoHistorialDao(database: SincMobileDatabase): MovimientoHistorialDao {
        return database.movimientoHistorialDao()
    }

    @Singleton
    @Provides
    fun provideDeclaracionVentaDao(database: SincMobileDatabase): DeclaracionVentaDao {
        return database.declaracionVentaDao()
    }

    // --- Proveedores de DAOs de Catálogos ---

    @Singleton
    @Provides
    fun provideEspecieDao(database: SincMobileDatabase): EspecieDao {
        return database.especieDao()
    }

    @Singleton
    @Provides
    fun provideRazaDao(database: SincMobileDatabase): RazaDao {
        return database.razaDao()
    }

    @Singleton
    @Provides
    fun provideCategoriaAnimalDao(database: SincMobileDatabase): CategoriaAnimalDao {
        return database.categoriaAnimalDao()
    }

    @Singleton
    @Provides
    fun provideMotivoMovimientoDao(database: SincMobileDatabase): MotivoMovimientoDao {
        return database.motivoMovimientoDao()
    }

    @Singleton
    @Provides
    fun provideMunicipioDao(database: SincMobileDatabase): MunicipioDao {
        return database.municipioDao()
    }

    @Singleton
    @Provides
    fun provideCondicionTenenciaDao(database: SincMobileDatabase): CondicionTenenciaDao {
        return database.condicionTenenciaDao()
    }

    @Singleton
    @Provides
    fun provideFuenteAguaDao(database: SincMobileDatabase): FuenteAguaDao {
        return database.fuenteAguaDao()
    }

    @Singleton
    @Provides
    fun provideTipoSueloDao(database: SincMobileDatabase): TipoSueloDao {
        return database.tipoSueloDao()
    }

    @Singleton
    @Provides
    fun provideTipoPastoDao(database: SincMobileDatabase): TipoPastoDao {
        return database.tipoPastoDao()
    }

    @Singleton
    @Provides
    fun provideIdentifierConfigDao(database: SincMobileDatabase): IdentifierConfigDao {
        return database.identifierConfigDao()
    }
}
