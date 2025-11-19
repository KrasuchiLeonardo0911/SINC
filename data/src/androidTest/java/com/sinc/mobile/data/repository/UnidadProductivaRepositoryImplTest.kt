package com.sinc.mobile.data.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import com.sinc.mobile.data.di.DatabaseModule
import com.sinc.mobile.data.di.NetworkModule
import com.sinc.mobile.data.local.SincMobileDatabase
import com.sinc.mobile.data.local.dao.UnidadProductivaDao
import com.sinc.mobile.data.network.api.UnidadProductivaApiService
import com.sinc.mobile.data.network.dto.response.UnidadProductivaDto
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
    private lateinit var apiService: UnidadProductivaApiService
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
            .create(UnidadProductivaApiService::class.java)

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

    @Test
    fun createUnidadProductiva_success_returnsSuccessAndInsertsIntoDb() = runBlocking {
        // Given
        val authToken = "test_token"
        sessionManager.saveAuthToken(authToken)

        val createData = com.sinc.mobile.domain.model.CreateUnidadProductivaData(
            nombre = "Nueva UP",
            identificadorLocal = "ID-123",
            superficie = 100.5,
            latitud = -34.0,
            longitud = -59.0,
            municipioId = 1,
            condicionTenenciaId = 1,
            fuenteAguaId = 1,
            tipoSueloId = 1,
            tipoPastoId = 1
        )

        val mockResponseDto = UnidadProductivaDto(
            id = 3,
            nombre = "Nueva UP",
            identificadorLocal = "ID-123",
            superficie = 100.5,
            latitud = "-34.0",
            longitud = "-59.0",
            municipioId = 1,
            condicionTenenciaId = 1,
            fuenteAguaId = 1,
            tipoSueloId = 1,
            tipoPastoId = 1
        )
        val jsonResponse = Gson().toJson(mockResponseDto)
        mockWebServer.enqueue(MockResponse().setResponseCode(201).setBody(jsonResponse))

        // When
        val result = repository.createUnidadProductiva(createData)
        mockWebServer.takeRequest() // Consume the request

        // Then
        assertThat(result.isSuccess).isTrue()
        val domainModel = result.getOrNull()
        assertThat(domainModel).isNotNull()
        assertThat(domainModel?.id).isEqualTo(3)
        assertThat(domainModel?.nombre).isEqualTo("Nueva UP")

        val ups = unidadProductivaDao.getAllUnidadesProductivas().first()
        assertThat(ups).hasSize(1)
        assertThat(ups[0].id).isEqualTo(3)
        assertThat(ups[0].nombre).isEqualTo("Nueva UP")
    }

    @Test
    fun createUnidadProductiva_apiError_returnsFailure() = runBlocking {
        // Given
        val authToken = "test_token"
        sessionManager.saveAuthToken(authToken)

        val createData = com.sinc.mobile.domain.model.CreateUnidadProductivaData(nombre = "Test")
        mockWebServer.enqueue(MockResponse().setResponseCode(500).setBody("{}"))

        // When
        val result = repository.createUnidadProductiva(createData)
        mockWebServer.takeRequest() // Consume the request

        // Then
        assertThat(result.isFailure).isTrue()
        val exception = result.exceptionOrNull()
        assertThat(exception).isNotNull()
        assertThat(exception).hasMessageThat().contains("Error de API")

        // Verify no data was inserted
        val ups = unidadProductivaDao.getAllUnidadesProductivas().first()
        assertThat(ups).isEmpty()
    }

    @Test
    fun createUnidadProductiva_validationError_returnsFailure() = runBlocking {
        // Given
        val authToken = "test_token"
        sessionManager.saveAuthToken(authToken)

        val createData = com.sinc.mobile.domain.model.CreateUnidadProductivaData(nombre = "Test")
        mockWebServer.enqueue(MockResponse().setResponseCode(422).setBody("{\"message\":\"Datos inválidos\"}"))

        // When
        val result = repository.createUnidadProductiva(createData)
        mockWebServer.takeRequest() // Consume the request

        // Then
        assertThat(result.isFailure).isTrue()
        val exception = result.exceptionOrNull()
        assertThat(exception).isNotNull()
        assertThat(exception).hasMessageThat().contains("Error de API")
        assertThat(exception).hasMessageThat().contains("422")

        // Verify no data was inserted
        val ups = unidadProductivaDao.getAllUnidadesProductivas().first()
        assertThat(ups).isEmpty()
    }

    @Test
    fun createUnidadProductiva_noAuthToken_returnsFailure() = runBlocking {
        // Given
        sessionManager.clearAuthToken()
        val createData = com.sinc.mobile.domain.model.CreateUnidadProductivaData(nombre = "Test")

        // When
        val result = repository.createUnidadProductiva(createData)

        // Then
        assertThat(result.isFailure).isTrue()
        val exception = result.exceptionOrNull()
        assertThat(exception).isNotNull()
        assertThat(exception).hasMessageThat().contains("No hay token de autenticación disponible")

        // Verify no request was made
        assertThat(mockWebServer.requestCount).isEqualTo(0)
    }
}
