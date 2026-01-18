package com.sinc.mobile.data.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.sinc.mobile.data.network.AuthInterceptor
import com.sinc.mobile.data.network.ErrorInterceptor
import com.sinc.mobile.data.network.api.AuthApiService
import com.sinc.mobile.data.network.api.MovimientoApiService
import com.sinc.mobile.data.network.api.TicketApiService
import com.sinc.mobile.data.network.api.UnidadProductivaApiService
import com.sinc.mobile.data.network.IdentifierApiService
import com.sinc.mobile.data.network.api.HistorialMovimientosApiService
import com.sinc.mobile.data.network.api.StockApiService
import com.sinc.mobile.data.network.api.VentasApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "https://sicsurmisiones.online/"

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        errorInterceptor: ErrorInterceptor
    ): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authInterceptor)
            .addInterceptor(errorInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, json: Json): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
        }

    @Provides
    @Singleton
    fun provideAuthApiService(retrofit: Retrofit): AuthApiService {
        return retrofit.create(AuthApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideMovimientoApiService(retrofit: Retrofit): MovimientoApiService {
        return retrofit.create(MovimientoApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideUnidadProductivaApiService(retrofit: Retrofit): UnidadProductivaApiService {
        return retrofit.create(UnidadProductivaApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideTicketApiService(retrofit: Retrofit): TicketApiService {
        return retrofit.create(TicketApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideStockApiService(retrofit: Retrofit): StockApiService {
        return retrofit.create(StockApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideHistorialMovimientosApiService(retrofit: Retrofit): HistorialMovimientosApiService {
        return retrofit.create(HistorialMovimientosApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideIdentifierApiService(retrofit: Retrofit): IdentifierApiService {
        return retrofit.create(IdentifierApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideVentasApiService(retrofit: Retrofit): VentasApiService {
        return retrofit.create(VentasApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideLogisticsApiService(retrofit: Retrofit): com.sinc.mobile.data.network.api.LogisticsApiService {
        return retrofit.create(com.sinc.mobile.data.network.api.LogisticsApiService::class.java)
    }
}

