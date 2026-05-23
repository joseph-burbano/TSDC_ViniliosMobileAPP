package com.uniandes.vinilos.repository

import com.uniandes.vinilos.database.dao.CollectorDao
import com.uniandes.vinilos.database.entities.CollectorEntity
import com.uniandes.vinilos.model.Collector
import com.uniandes.vinilos.model.Performer
import com.uniandes.vinilos.network.VinilosApi
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

class CollectorRepositoryTest {

    private lateinit var dao: CollectorDao
    private lateinit var api: VinilosApi
    private lateinit var repository: CollectorRepository

    @Before
    fun setup() {
        dao = mockk(relaxed = true)
        api = mockk(relaxed = true)
        repository = CollectorRepository(dao, api)
    }

    @Test
    fun `getCollectors devuelve cache cuando el DAO no esta vacio`() = runTest {
        coEvery { dao.getAll() } returns listOf(MANOLO_ENTITY)

        val result = repository.getCollectors()

        assertEquals(1, result.size)
        assertEquals("Manolo Bellon", result[0].name)
        coVerify(exactly = 0) { api.getCollectors() }
        coVerify(exactly = 0) { dao.insertAll(any()) }
    }

    @Test
    fun `getCollectors llama al API cuando el cache esta vacio y persiste el resultado`() = runTest {
        coEvery { dao.getAll() } returns emptyList()
        coEvery { api.getCollectors() } returns listOf(MANOLO_REMOTE)

        val result = repository.getCollectors()

        assertEquals(1, result.size)
        assertEquals("Manolo Bellon", result.first().name)
        coVerify(exactly = 1) { api.getCollectors() }
        coVerify(exactly = 1) { dao.insertAll(any()) }
    }

    @Test
    fun `getCollectors conserva los favoritePerformers del API`() = runTest {
        coEvery { dao.getAll() } returns emptyList()
        coEvery { api.getCollectors() } returns listOf(MANOLO_REMOTE)

        val collector = repository.getCollectors().first()

        assertEquals(1, collector.favoritePerformers.size)
        assertEquals("Rubén Blades Bellido de Luna", collector.favoritePerformers.first().name)
    }

    @Test
    fun `refreshCollectors borra el cache y vuelve a llamar al API`() = runTest {
        coEvery { api.getCollectors() } returns listOf(MANOLO_REMOTE)

        repository.refreshCollectors()

        coVerify(exactly = 1) { dao.deleteAll() }
        coVerify(exactly = 1) { api.getCollectors() }
        coVerify(exactly = 1) { dao.insertAll(any()) }
    }

    @Test
    fun `getCollectors devuelve lista vacia cuando el API devuelve vacio`() = runTest {
        coEvery { dao.getAll() } returns emptyList()
        coEvery { api.getCollectors() } returns emptyList()

        val result = repository.getCollectors()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `getCollector devuelve desde cache si existe`() = runTest {
        coEvery { dao.findById(100) } returns MANOLO_ENTITY

        val result = repository.getCollector(100)

        assertEquals("Manolo Bellon", result?.name)
        coVerify(exactly = 0) { api.getCollector(any()) }
    }

    @Test
    fun `getCollector llama al API si no esta en cache y persiste`() = runTest {
        coEvery { dao.findById(100) } returns null
        coEvery { api.getCollector(100) } returns MANOLO_REMOTE

        val result = repository.getCollector(100)

        assertEquals("Manolo Bellon", result?.name)
        coVerify(exactly = 1) { api.getCollector(100) }
        coVerify(exactly = 1) { dao.insertAll(any()) }
    }

    @Test
    fun `getCollector devuelve null si el API falla`() = runTest {
        coEvery { dao.findById(999) } returns null
        coEvery { api.getCollector(999) } throws IOException("sin red")

        val result = repository.getCollector(999)

        assertNull(result)
    }

    @Test
    fun `getCollectors propaga las excepciones del API`() = runTest {
        coEvery { dao.getAll() } returns emptyList()
        coEvery { api.getCollectors() } throws IOException("sin red")

        val thrown = org.junit.Assert.assertThrows(IOException::class.java) {
            kotlinx.coroutines.runBlocking { repository.getCollectors() }
        }
        assertEquals("sin red", thrown.message)
    }

    // ── addFavoritePerformer ──────────────────────────────────────────────────

    @Test
    fun `addFavoritePerformer llama al endpoint de musico y retorna el artista agregado`() = runTest {
        coEvery { dao.findById(100) } returns MANOLO_ENTITY
        coEvery { api.addFavoriteMusician(100, RUBEN_PERFORMER.id) } returns RUBEN_PERFORMER

        val result = repository.addFavoritePerformer(100, RUBEN_PERFORMER)

        assertEquals(RUBEN_PERFORMER.name, result.name)
        coVerify(exactly = 1) { api.addFavoriteMusician(100, RUBEN_PERFORMER.id) }
        coVerify(exactly = 0) { api.addFavoriteBand(any(), any()) }
    }

    @Test
    fun `addFavoritePerformer llama al endpoint de banda cuando el performer es banda`() = runTest {
        coEvery { dao.findById(100) } returns MANOLO_ENTITY
        coEvery { api.addFavoriteBand(100, BEATLES_BAND.id) } returns BEATLES_BAND

        repository.addFavoritePerformer(100, BEATLES_BAND)

        coVerify(exactly = 1) { api.addFavoriteBand(100, BEATLES_BAND.id) }
        coVerify(exactly = 0) { api.addFavoriteMusician(any(), any()) }
    }

    @Test
    fun `addFavoritePerformer actualiza la cache de Room con el nuevo favorito`() = runTest {
        coEvery { dao.findById(100) } returns MANOLO_ENTITY
        coEvery { api.addFavoriteMusician(100, RUBEN_PERFORMER.id) } returns RUBEN_PERFORMER

        repository.addFavoritePerformer(100, RUBEN_PERFORMER)

        coVerify(exactly = 1) { dao.insertAll(match { entities ->
            entities.any { it.id == 100 && it.favoritePerformers.any { p -> p.id == RUBEN_PERFORMER.id } }
        }) }
    }

    // ── removeFavoritePerformer ───────────────────────────────────────────────

    @Test
    fun `removeFavoritePerformer llama al endpoint de musico y actualiza la cache`() = runTest {
        val entityWithFavorite = MANOLO_ENTITY.copy(favoritePerformers = listOf(RUBEN_PERFORMER))
        coEvery { dao.findById(100) } returns entityWithFavorite

        repository.removeFavoritePerformer(100, RUBEN_PERFORMER)

        coVerify(exactly = 1) { api.removeFavoriteMusician(100, RUBEN_PERFORMER.id) }
        coVerify(exactly = 0) { api.removeFavoriteBand(any(), any()) }
        coVerify(exactly = 1) { dao.insertAll(match { entities ->
            entities.any { it.id == 100 && it.favoritePerformers.none { p -> p.id == RUBEN_PERFORMER.id } }
        }) }
    }

    @Test
    fun `removeFavoritePerformer llama al endpoint de banda cuando el performer es banda`() = runTest {
        val entityWithBand = MANOLO_ENTITY.copy(favoritePerformers = listOf(BEATLES_BAND))
        coEvery { dao.findById(100) } returns entityWithBand

        repository.removeFavoritePerformer(100, BEATLES_BAND)

        coVerify(exactly = 1) { api.removeFavoriteBand(100, BEATLES_BAND.id) }
        coVerify(exactly = 0) { api.removeFavoriteMusician(any(), any()) }
    }

    // ── preservación de favoritos en refresh ──────────────────────────────────

    @Test
    fun `refreshCollectors preserva favoritePerformers locales cuando el API devuelve lista sin favoritos`() = runTest {
        val entityWithFavorite = MANOLO_ENTITY.copy(favoritePerformers = listOf(RUBEN_PERFORMER))
        coEvery { dao.getAll() } returns listOf(entityWithFavorite)
        val remoteWithoutFavorites = MANOLO_REMOTE.copy(favoritePerformers = emptyList())
        coEvery { api.getCollectors() } returns listOf(remoteWithoutFavorites)

        val result = repository.refreshCollectors()

        assertEquals(1, result.first().favoritePerformers.size)
        assertEquals(RUBEN_PERFORMER.name, result.first().favoritePerformers.first().name)
    }

    @Test
    fun `refreshCollectors no sobreescribe favoritos cuando el API los trae`() = runTest {
        coEvery { dao.getAll() } returns listOf(MANOLO_ENTITY)
        coEvery { api.getCollectors() } returns listOf(MANOLO_REMOTE)

        val result = repository.refreshCollectors()

        // MANOLO_REMOTE ya tiene favoritePerformers, deben respetarse
        assertEquals(1, result.first().favoritePerformers.size)
        assertEquals(RUBEN_PERFORMER.name, result.first().favoritePerformers.first().name)
    }

    private companion object {
        val RUBEN_PERFORMER = Performer(
            id = 100,
            name = "Rubén Blades Bellido de Luna",
            image = "https://img/ruben.png",
            description = "Cantante panameño",
            birthDate = "1948-07-16",
            type = "musician"
        )
        val BEATLES_BAND = Performer(
            id = 200,
            name = "The Beatles",
            image = "https://img/beatles.png",
            description = "Rock band",
            creationDate = "1960-01-01",
            type = "band"
        )
        val MANOLO_REMOTE = Collector(
            id = 100,
            name = "Manolo Bellon",
            telephone = "3502457896",
            email = "manollo@caracol.com.co",
            favoritePerformers = listOf(RUBEN_PERFORMER)
        )

        val MANOLO_ENTITY = CollectorEntity(
            id = 100,
            name = "Manolo Bellon",
            telephone = "3502457896",
            email = "manollo@caracol.com.co"
        )
    }
}
