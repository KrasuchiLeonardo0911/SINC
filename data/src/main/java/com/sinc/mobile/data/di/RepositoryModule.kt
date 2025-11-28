package com.sinc.mobile.data.di

import com.sinc.mobile.data.repository.AuthRepositoryImpl
import com.sinc.mobile.data.repository.UnidadProductivaRepositoryImpl
import com.sinc.mobile.domain.repository.AuthRepository
import com.sinc.mobile.domain.repository.UnidadProductivaRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

import com.sinc.mobile.data.repository.CatalogosRepositoryImpl
import com.sinc.mobile.domain.repository.CatalogosRepository

import com.sinc.mobile.data.repository.MovimientoRepositoryImpl
import com.sinc.mobile.domain.repository.MovimientoRepository

import com.sinc.mobile.data.repository.LocationRepositoryImpl
import com.sinc.mobile.domain.repository.LocationRepository

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindUnidadProductivaRepository(impl: UnidadProductivaRepositoryImpl): UnidadProductivaRepository

    @Binds
    @Singleton
    abstract fun bindCatalogosRepository(impl: CatalogosRepositoryImpl): CatalogosRepository

    @Binds
    @Singleton
    abstract fun bindMovimientoRepository(impl: MovimientoRepositoryImpl): MovimientoRepository

    @Binds
    @Singleton
    abstract fun bindLocationRepository(impl: LocationRepositoryImpl): LocationRepository
}
