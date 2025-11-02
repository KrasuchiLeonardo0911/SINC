package com.sinc.mobile.data.local.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.sinc.mobile.data.local.SincMobileDatabase
import com.sinc.mobile.data.local.entities.UnidadProductivaEntity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

import com.sinc.mobile.data.di.DatabaseModule
import dagger.hilt.android.testing.UninstallModules

@UninstallModules(DatabaseModule::class)
@HiltAndroidTest
class UnidadProductivaDaoTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Inject
    lateinit var database: SincMobileDatabase

    @Inject
    lateinit var unidadProductivaDao: UnidadProductivaDao

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertAllAndGetAllUnidadesProductivas() = runTest {
        val unidades = listOf(
            UnidadProductivaEntity(1, "UP1", "ID1", 1.0, 1.0, 1, 1, true, true),
            UnidadProductivaEntity(2, "UP2", "ID2", 2.0, 2.0, 2, 2, false, true)
        )
        unidadProductivaDao.insertAll(unidades)

        val allUnidades = unidadProductivaDao.getAllUnidadesProductivas().first()
        assertThat(allUnidades).containsExactlyElementsIn(unidades)
    }

    @Test
    fun clearAllUnidadesProductivas() = runTest {
        val unidades = listOf(
            UnidadProductivaEntity(1, "UP1", "ID1", 1.0, 1.0, 1, 1, true, true)
        )
        unidadProductivaDao.insertAll(unidades)

        unidadProductivaDao.clearAll()

        val allUnidades = unidadProductivaDao.getAllUnidadesProductivas().first()
        assertThat(allUnidades).isEmpty()
    }

    @Test
    fun insertOnConflictReplacesExisting() = runTest {
        val unidad1 = UnidadProductivaEntity(1, "UP1", "ID1", 1.0, 1.0, 1, 1, true, true)
        unidadProductivaDao.insertAll(listOf(unidad1))

        val updatedUnidad1 = UnidadProductivaEntity(1, "UP1 Updated", "ID1", 1.1, 1.1, 1, 1, false, false)
        unidadProductivaDao.insertAll(listOf(updatedUnidad1))

        val allUnidades = unidadProductivaDao.getAllUnidadesProductivas().first()
        assertThat(allUnidades).containsExactly(updatedUnidad1)
        assertThat(allUnidades.size).isEqualTo(1)
    }
}
