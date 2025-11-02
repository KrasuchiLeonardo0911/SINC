package com.sinc.mobile.data.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.sinc.mobile.data.local.SincMobileDatabase
import com.sinc.mobile.data.local.dao.CategoriaAnimalDao
import com.sinc.mobile.data.local.dao.EspecieDao
import com.sinc.mobile.data.local.dao.MotivoMovimientoDao
import com.sinc.mobile.data.local.dao.MovimientoPendienteDao
import com.sinc.mobile.data.local.dao.RazaDao
import com.sinc.mobile.data.local.dao.UnidadProductivaDao
import com.sinc.mobile.data.session.SessionManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent


@Module
@InstallIn(SingletonComponent::class)
object TestAppModule {



    @Provides
    fun provideInMemoryDb(@ApplicationContext context: Context): SincMobileDatabase {
        return Room.inMemoryDatabaseBuilder(
            context,
            SincMobileDatabase::class.java
        )
        .addCallback(object : RoomDatabase.Callback() {
            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                db.execSQL("PRAGMA foreign_keys=OFF")
            }
        })
        .allowMainThreadQueries()
        .build()
    }

    @Provides
    fun provideEspecieDao(db: SincMobileDatabase): EspecieDao = db.especieDao()

    @Provides
    fun provideRazaDao(db: SincMobileDatabase): RazaDao = db.razaDao()

    @Provides
    fun provideCategoriaAnimalDao(db: SincMobileDatabase): CategoriaAnimalDao = db.categoriaAnimalDao()

    @Provides
    fun provideMotivoMovimientoDao(db: SincMobileDatabase): MotivoMovimientoDao = db.motivoMovimientoDao()

    @Provides
    fun provideMovimientoPendienteDao(db: SincMobileDatabase): MovimientoPendienteDao = db.movimientoPendienteDao()

    @Provides
    fun provideUnidadProductivaDao(db: SincMobileDatabase): UnidadProductivaDao = db.unidadProductivaDao()

    @Provides
    fun provideSessionManager(@ApplicationContext context: Context): SessionManager {
        return SessionManager(context)
    }


}