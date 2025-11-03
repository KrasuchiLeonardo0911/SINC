package com.sinc.mobile.data.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import com.sinc.mobile.data.di.DatabaseModule
import com.sinc.mobile.data.di.NetworkModule
import com.sinc.mobile.data.local.SincMobileDatabase
import com.sinc.mobile.data.local.dao.UnidadProductivaDao
import com.sinc.mobile.data.network.api.AuthApiService
import com.sinc.mobile.data.network.dto.UnidadProductivaDto
import com.sinc.mobile.data.session.SessionManager
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject

@UninstallModules(DatabaseModule::class, NetworkModule::class)
@HiltAndroidTest
@ExperimentalCoroutinesApi
class UnidadProductivaRepositoryImplTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var mockWebServer: MockWebServer
    private lateinit var apiService: AuthApiService
    private lateinit var repository: UnidadProductivaRepositoryImpl

    @Inject
    lateinit var db: SincMobileDatabase

    @Inject
    lateinit var unidadProductivaDao: UnidadProductivaDao

    @Inject
    lateinit var sessionManager: SessionManager

    @Before
    fun setup() {
        hiltRule.inject()

        mockWebServer = MockWebServer()
        mockWebServer.start()

        val okHttpClient = OkHttpClient.Builder().build()

        apiService = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AuthApiService::class.java)

        repository = UnidadProductivaRepositoryImpl(
            apiService,
            sessionManager,
            unidadProductivaDao
        )
    }

    @After
    fun teardown() {
        mockWebServer.shutdown()
        db.close()
    }

    @Test
    fun syncUnidadesProductivas_success_insertsDataIntoDb() = runBlocking {
        // Given
        val authToken = "test_token"
        sessionManager.saveAuthToken(authToken)

        val mockUpDto = listOf(
            UnidadProductivaDto(1, "UP 1", "-34.0", "-59.0"),
            UnidadProductivaDto(2, "UP 2", "-35.0", "-60.0")
        )
        val jsonResponse = Gson().toJson(mockUpDto)
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(jsonResponse))

        // When
        val result = repository.syncUnidadesProductivas()
        mockWebServer.takeRequest() // Consume the request

        // Then
        assertThat(result.isSuccess).isTrue()

        val ups = unidadProductivaDao.getAllUnidadesProductivas().first()
        assertThat(ups).hasSize(2)
        assertThat(ups[0].nombre).isEqualTo("UP 1")
        assertThat(ups[1].id).isEqualTo(2)
    }

    @Test
    fun syncUnidadesProductivas_apiError_returnsFailure() = runBlocking {
        // Given
        val authToken = "test_token"
        sessionManager.saveAuthToken(authToken)

        mockWebServer.enqueue(MockResponse().setResponseCode(500).setBody("{}"))

        // When
        val result = repository.syncUnidadesProductivas()
        mockWebServer.takeRequest() // Consume the request

        // Then
        assertThat(result.isFailure).isTrue()
        val exception = result.exceptionOrNull()
        assertThat(exception).isNotNull()
        assertThat(exception).hasMessageThat().contains("Error de API")

        // Verify no data was inserted
        assertThat(unidadProductivaDao.getAllUnidadesProductivas().first()).isEmpty()
    }

    @Test
    fun syncUnidadesProductivas_noAuthToken_returnsFailure() = runBlocking {
        // Given
        sessionManager.clearAuthToken()

        // When
        val result = repository.syncUnidadesProductivas()

        // Then
        assertThat(result.isFailure).isTrue()
        val exception = result.exceptionOrNull()
        assertThat(exception).isNotNull()
        assertThat(exception).hasMessageThat().contains("No hay token de autenticación disponible")

        // Verify no data was inserted
        assertThat(unidadProductivaDao.getAllUnidadesProductivas().first()).isEmpty()
    }
}
