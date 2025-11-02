package com.sinc.mobile.data.di

import android.content.Context
import androidx.room.Room
import com.sinc.mobile.data.local.SincMobileDatabase
import com.sinc.mobile.data.local.dao.CategoriaAnimalDao
import com.sinc.mobile.data.local.dao.EspecieDao
import com.sinc.mobile.data.local.dao.MotivoMovimientoDao
import com.sinc.mobile.data.local.dao.MovimientoPendienteDao
import com.sinc.mobile.data.local.dao.RazaDao
import com.sinc.mobile.data.local.dao.UnidadProductivaDao
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
        ).build()
    }

    @Singleton
    @Provides
    fun provideUnidadProductivaDao(database: SincMobileDatabase): UnidadProductivaDao {
        return database.unidadProductivaDao()
    }

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
    fun provideMovimientoPendienteDao(database: SincMobileDatabase): MovimientoPendienteDao {
        return database.movimientoPendienteDao()
    }
}
