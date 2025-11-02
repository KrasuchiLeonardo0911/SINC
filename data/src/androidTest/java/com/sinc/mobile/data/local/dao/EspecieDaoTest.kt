package com.sinc.mobile.data.local.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.sinc.mobile.data.local.SincMobileDatabase
import com.sinc.mobile.data.local.entities.EspecieEntity // Changed import
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
class EspecieDaoTest { // Changed class name

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Inject
    lateinit var database: SincMobileDatabase

    @Inject
    lateinit var especieDao: EspecieDao // Changed injected DAO

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertAllAndGetAllEspecies() = runTest { // Changed method name
        val especies = listOf(
            EspecieEntity(1, "Ovino"),
            EspecieEntity(2, "Caprino")
        )
        especieDao.insertAll(especies)

        val allEspecies = especieDao.getAllEspecies().first() // Changed DAO method
        assertThat(allEspecies).containsExactlyElementsIn(especies)
    }

    @Test
    fun clearAllEspecies() = runTest { // Changed method name
        val especies = listOf(
            EspecieEntity(1, "Ovino")
        )
        especieDao.insertAll(especies)

        especieDao.clearAll() // Changed DAO method

        val allEspecies = especieDao.getAllEspecies().first() // Changed DAO method
        assertThat(allEspecies).isEmpty()
    }

    @Test
    fun insertOnConflictReplacesExisting() = runTest {
        val especie1 = EspecieEntity(1, "Ovino")
        especieDao.insertAll(listOf(especie1))

        val updatedEspecie1 = EspecieEntity(1, "Ovino Actualizado")
        especieDao.insertAll(listOf(updatedEspecie1))

        val allEspecies = especieDao.getAllEspecies().first()
        assertThat(allEspecies).containsExactly(updatedEspecie1)
        assertThat(allEspecies.size).isEqualTo(1)
    }
}
