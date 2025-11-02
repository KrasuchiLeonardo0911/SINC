package com.sinc.mobile.data.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.gson.Gson
import com.google.common.truth.Truth.assertThat
import com.sinc.mobile.data.local.SincMobileDatabase
import com.sinc.mobile.data.local.dao.CategoriaAnimalDao
import com.sinc.mobile.data.local.dao.EspecieDao
import com.sinc.mobile.data.local.dao.MotivoMovimientoDao
import com.sinc.mobile.data.local.dao.RazaDao
import com.sinc.mobile.data.network.api.AuthApiService
import com.sinc.mobile.data.network.dto.CatalogosDto
import com.sinc.mobile.data.network.dto.CategoriaDto
import com.sinc.mobile.data.network.dto.EspecieDto
import com.sinc.mobile.data.network.dto.MotivoMovimientoDto
import com.sinc.mobile.data.network.dto.RazaDto
import com.sinc.mobile.data.di.DatabaseModule
import com.sinc.mobile.data.di.NetworkModule
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
class
CatalogosRepositoryImplTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var mockWebServer: MockWebServer
    private lateinit var apiService: AuthApiService
    private lateinit var repository: CatalogosRepositoryImpl

    @Inject
    lateinit var db: SincMobileDatabase

    @Inject
    lateinit var especieDao: EspecieDao

    @Inject
    lateinit var razaDao: RazaDao

    @Inject
    lateinit var categoriaAnimalDao: CategoriaAnimalDao

    @Inject
    lateinit var motivoMovimientoDao: MotivoMovimientoDao

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

        repository = CatalogosRepositoryImpl(
            apiService,
            sessionManager,
            especieDao,
            razaDao,
            categoriaAnimalDao,
            motivoMovimientoDao
        )
    }

    @After
    fun teardown() {
        mockWebServer.shutdown()
        if (this::db.isInitialized) {
            db.close()
        }
    }

    @Test
    fun syncCatalogos_success_insertsDataIntoDb() = runBlocking {
        // Given
        val authToken = "test_token"
        sessionManager.saveAuthToken(authToken)

        val mockCatalogosDto = CatalogosDto(
            especies = listOf(
                EspecieDto(1, "Ovino"),
                EspecieDto(2, "Caprino")
            ),
            razas = listOf(
                RazaDto(1, "Merino", 1),
                RazaDto(2, "Criolla", 2)
            ),
            categorias = listOf(
                CategoriaDto(1, "Cordero/a", 1),
                CategoriaDto(2, "Cabrito/a", 2)
            ),
            motivosMovimiento = listOf(
                MotivoMovimientoDto(1, "Nacimiento", "alta"),
                MotivoMovimientoDto(2, "Venta", "baja")
            )
        )
        val jsonResponse = Gson().toJson(mockCatalogosDto)
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(jsonResponse))

        // When
        val result = repository.syncCatalogos()
        mockWebServer.takeRequest() // Consume the request

        // Then
        if (result.isFailure) {
            throw result.exceptionOrNull()!!
        }
        assertThat(result.isSuccess).isTrue()

        val especies = especieDao.getAllEspecies().first()
        assertThat(especies).hasSize(2)
        assertThat(especies[0].nombre).isEqualTo("Ovino")
        assertThat(especies[1].nombre).isEqualTo("Caprino")

        val razas = razaDao.getAllRazas().first()
        assertThat(razas).hasSize(2)
        assertThat(razas[0].nombre).isEqualTo("Merino")
        assertThat(razas[1].nombre).isEqualTo("Criolla")

        val categorias = categoriaAnimalDao.getAllCategorias().first()
        assertThat(categorias).hasSize(2)
        assertThat(categorias[0].nombre).isEqualTo("Cordero/a")
        assertThat(categorias[1].nombre).isEqualTo("Cabrito/a")

        val motivos = motivoMovimientoDao.getAllMotivosMovimiento().first()
        assertThat(motivos).hasSize(2)
        assertThat(motivos[0].nombre).isEqualTo("Nacimiento")
        assertThat(motivos[1].nombre).isEqualTo("Venta")
    }

    @Test
    fun syncCatalogos_apiError_returnsFailure() = runBlocking {
        // Given
        val authToken = "test_token"
        sessionManager.saveAuthToken(authToken)

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(500)
                .setHeader("Content-Type", "application/json; charset=utf-8")
                .setBody("{\"message\": \"Internal Server Error\"}")
        )

        // When
        val result = repository.syncCatalogos()
        mockWebServer.takeRequest() // Consume the request

        // Then
        assertThat(result.isFailure).isTrue()
        val exception = result.exceptionOrNull()
        assertThat(exception).isNotNull()
        assertThat(exception).hasMessageThat().contains("Error de API")
        assertThat(exception).hasMessageThat().contains("500")
        assertThat(exception).hasMessageThat().contains("Internal Server Error")

        // Verify no data was inserted
        assertThat(especieDao.getAllEspecies().first()).isEmpty()
        assertThat(razaDao.getAllRazas().first()).isEmpty()
        assertThat(categoriaAnimalDao.getAllCategorias().first()).isEmpty()
        assertThat(motivoMovimientoDao.getAllMotivosMovimiento().first()).isEmpty()
    }

    @Test
    fun syncCatalogos_noAuthToken_returnsFailure() = runBlocking {
        // Given
        sessionManager.clearAuthToken() // Ensure no token is present

        // When
        val result = repository.syncCatalogos()

        // Then
        assertThat(result.isFailure).isTrue()
        val exception = result.exceptionOrNull()
        assertThat(exception).isNotNull()
        assertThat(exception).hasMessageThat().contains("No hay token de autenticación disponible para sincronizar catálogos.")

        // Verify no data was inserted
        assertThat(especieDao.getAllEspecies().first()).isEmpty()
        assertThat(razaDao.getAllRazas().first()).isEmpty()
        assertThat(categoriaAnimalDao.getAllCategorias().first()).isEmpty()
        assertThat(motivoMovimientoDao.getAllMotivosMovimiento().first()).isEmpty()
    }
}
