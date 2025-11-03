package com.sinc.mobile.app.di

import com.sinc.mobile.app.navigation.AppNavigationManager
import com.sinc.mobile.domain.navigation.NavigationManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    abstract fun bindNavigationManager(impl: AppNavigationManager): NavigationManager
}
