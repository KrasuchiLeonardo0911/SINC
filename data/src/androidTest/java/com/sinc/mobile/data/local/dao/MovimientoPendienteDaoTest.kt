package com.sinc.mobile.data.local.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.sinc.mobile.data.local.SincMobileDatabase
import com.sinc.mobile.data.local.entities.* // Import all entities
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDateTime
import javax.inject.Inject

import com.sinc.mobile.data.di.DatabaseModule
import dagger.hilt.android.testing.UninstallModules

@UninstallModules(DatabaseModule::class)
@HiltAndroidTest
class MovimientoPendienteDaoTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Inject
    lateinit var database: SincMobileDatabase

    @Inject
    lateinit var movimientoPendienteDao: MovimientoPendienteDao

    // Inject all parent DAOs for foreign key setup
    @Inject lateinit var unidadProductivaDao: UnidadProductivaDao
    @Inject lateinit var especieDao: EspecieDao
    @Inject lateinit var categoriaAnimalDao: CategoriaAnimalDao
    @Inject lateinit var motivoMovimientoDao: MotivoMovimientoDao
    @Inject lateinit var razaDao: RazaDao

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @After
    fun teardown() {
        database.close()
    }

    private suspend fun setupForeignKeys() {
        val up = UnidadProductivaEntity(1, "UP Test", "ID1", null, null, null, null, true, true)
        unidadProductivaDao.insertAll(listOf(up))

        val especie = EspecieEntity(1, "Ovino")
        especieDao.insertAll(listOf(especie))

        val raza = RazaEntity(1, 1, "Merino")
        razaDao.insertAll(listOf(raza))

        val categoria = CategoriaAnimalEntity(1, 1, "Cordero")
        categoriaAnimalDao.insertAll(listOf(categoria))

        val motivo = MotivoMovimientoEntity(1, "Nacimiento", "alta")
        motivoMovimientoDao.insertAll(listOf(motivo))
    }

    @Test
    fun insertAllAndGetAllMovimientosPendientes() = runTest {
        setupForeignKeys()

        val originalMovimientos = listOf(
            MovimientoPendienteEntity(
                unidad_productiva_id = 1,
                especie_id = 1,
                categoria_id = 1,
                raza_id = 1,
                cantidad = 10,
                motivo_movimiento_id = 1,
                destino_traslado = null,
                fecha_registro = LocalDateTime.now(),
                sincronizado = false
            )
        )
        movimientoPendienteDao.insertAll(originalMovimientos)

        val allMovimientos = movimientoPendienteDao.getAllMovimientosPendientes().first()
        assertThat(allMovimientos).hasSize(originalMovimientos.size)
        assertThat(allMovimientos[0].unidad_productiva_id).isEqualTo(originalMovimientos[0].unidad_productiva_id)
        assertThat(allMovimientos[0].especie_id).isEqualTo(originalMovimientos[0].especie_id)
        assertThat(allMovimientos[0].categoria_id).isEqualTo(originalMovimientos[0].categoria_id)
        assertThat(allMovimientos[0].raza_id).isEqualTo(originalMovimientos[0].raza_id)
        assertThat(allMovimientos[0].cantidad).isEqualTo(originalMovimientos[0].cantidad)
        assertThat(allMovimientos[0].motivo_movimiento_id).isEqualTo(originalMovimientos[0].motivo_movimiento_id)
        assertThat(allMovimientos[0].destino_traslado).isEqualTo(originalMovimientos[0].destino_traslado)
        // Compare LocalDateTime fields with a tolerance or by converting to String if exact match is problematic
        assertThat(allMovimientos[0].fecha_registro.toLocalDate()).isEqualTo(originalMovimientos[0].fecha_registro.toLocalDate())
        assertThat(allMovimientos[0].sincronizado).isEqualTo(originalMovimientos[0].sincronizado)
    }

    @Test
    fun clearAllMovimientosPendientes() = runTest {
        setupForeignKeys()

        val movimientos = listOf(
            MovimientoPendienteEntity(
                unidad_productiva_id = 1,
                especie_id = 1,
                categoria_id = 1,
                raza_id = 1,
                cantidad = 5,
                motivo_movimiento_id = 1,
                destino_traslado = null,
                fecha_registro = LocalDateTime.now(),
                sincronizado = false
            )
        )
        movimientoPendienteDao.insertAll(movimientos)

        movimientoPendienteDao.clearAll()

        val allMovimientos = movimientoPendienteDao.getAllMovimientosPendientes().first()
        assertThat(allMovimientos).isEmpty()
    }

    @Test
    fun insertOnConflictReplacesExisting() = runTest {
        setupForeignKeys()

        val movimiento1 = MovimientoPendienteEntity(
            unidad_productiva_id = 1,
            especie_id = 1,
            categoria_id = 1,
            raza_id = 1,
            cantidad = 10,
            motivo_movimiento_id = 1,
            destino_traslado = null,
            fecha_registro = LocalDateTime.now(),
            sincronizado = false
        )
        movimientoPendienteDao.insertAll(listOf(movimiento1))

        // Retrieve the inserted entity to get its auto-generated ID
        val insertedMovimiento = movimientoPendienteDao.getAllMovimientosPendientes().first().first()
        val updatedMovimiento1 = insertedMovimiento.copy(cantidad = 20, sincronizado = true)
        movimientoPendienteDao.insertAll(listOf(updatedMovimiento1))

        val allMovimientos = movimientoPendienteDao.getAllMovimientosPendientes().first()
        assertThat(allMovimientos).hasSize(1)
        assertThat(allMovimientos[0].id).isEqualTo(updatedMovimiento1.id)
        assertThat(allMovimientos[0].cantidad).isEqualTo(updatedMovimiento1.cantidad)
        assertThat(allMovimientos[0].sincronizado).isTrue()
    }
}
