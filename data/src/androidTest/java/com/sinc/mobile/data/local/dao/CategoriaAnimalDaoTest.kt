package com.sinc.mobile.data.local.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.sinc.mobile.data.local.SincMobileDatabase
import com.sinc.mobile.data.local.entities.EspecieEntity
import com.sinc.mobile.data.local.entities.CategoriaAnimalEntity
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
class CategoriaAnimalDaoTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Inject
    lateinit var database: SincMobileDatabase

    @Inject
    lateinit var categoriaAnimalDao: CategoriaAnimalDao

    @Inject
    lateinit var especieDao: EspecieDao

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertAllAndGetAllCategoriasAnimal() = runTest {
        val especie1 = EspecieEntity(1, "Ovino")
        especieDao.insertAll(listOf(especie1))

        val categorias = listOf(
            CategoriaAnimalEntity(1, 1, "Cordero/a"),
            CategoriaAnimalEntity(2, 1, "Borrego/a (1-2 años)")
        )
        categoriaAnimalDao.insertAll(categorias)

        val allCategorias = categoriaAnimalDao.getAllCategorias().first()
        assertThat(allCategorias).containsExactlyElementsIn(categorias)
    }

    @Test
    fun clearAllCategoriasAnimal() = runTest {
        val especie1 = EspecieEntity(1, "Ovino")
        especieDao.insertAll(listOf(especie1))

        val categorias = listOf(
            CategoriaAnimalEntity(1, 1, "Cordero/a")
        )
        categoriaAnimalDao.insertAll(categorias)

        categoriaAnimalDao.clearAll()

        val allCategorias = categoriaAnimalDao.getAllCategorias().first()
        assertThat(allCategorias).isEmpty()
    }

    @Test
    fun insertOnConflictReplacesExisting() = runTest {
        val especie1 = EspecieEntity(1, "Ovino")
        especieDao.insertAll(listOf(especie1))

        val categoria1 = CategoriaAnimalEntity(1, 1, "Cordero/a")
        categoriaAnimalDao.insertAll(listOf(categoria1))

        val updatedCategoria1 = CategoriaAnimalEntity(1, 1, "Cordero/a Actualizado")
        categoriaAnimalDao.insertAll(listOf(updatedCategoria1))

        val allCategorias = categoriaAnimalDao.getAllCategorias().first()
        assertThat(allCategorias).containsExactly(updatedCategoria1)
        assertThat(allCategorias.size).isEqualTo(1)
    }
}
