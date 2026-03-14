package com.sinc.mobile.data.di

import com.sinc.mobile.data.util.TimeManagerImpl
import com.sinc.mobile.domain.util.TimeManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class UtilityModule {

    @Binds
    @Singleton
    abstract fun bindTimeManager(timeManagerImpl: TimeManagerImpl): TimeManager
}
