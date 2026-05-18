package com.uniandes.vinilos.repository

import com.uniandes.vinilos.database.dao.AlbumDao
import com.uniandes.vinilos.database.entities.AlbumEntity
import com.uniandes.vinilos.model.Album
import com.uniandes.vinilos.model.CreateTrackRequest
import com.uniandes.vinilos.model.Performer
import com.uniandes.vinilos.model.Track
import com.uniandes.vinilos.network.VinilosApi
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

class AlbumRepositoryTest {

    private lateinit var dao: AlbumDao
    private lateinit var api: VinilosApi
    private lateinit var repository: AlbumRepository

    @Before
    fun setup() {
        dao = mockk(relaxed = true)
        api = mockk(relaxed = true)
        repository = AlbumRepository(dao, api)
    }

    @Test
    fun `getAlbums devuelve cache cuando el DAO no esta vacio`() = runTest {
        coEvery { dao.getAll() } returns listOf(ABBEY_ROAD_ENTITY)

        val result = repository.getAlbums()

        assertEquals(1, result.size)
        assertEquals("Abbey Road", result[0].name)
        coVerify(exactly = 0) { api.getAlbums() }
        coVerify(exactly = 0) { dao.insertAll(any()) }
    }

    @Test
    fun `getAlbums llama al API cuando el cache esta vacio y persiste el resultado`() = runTest {
        coEvery { dao.getAll() } returns emptyList()
        coEvery { api.getAlbums() } returns listOf(ABBEY_ROAD_REMOTE)

        val result = repository.getAlbums()

        assertEquals(1, result.size)
        assertEquals("Abbey Road", result.first().name)
        coVerify(exactly = 1) { api.getAlbums() }
        coVerify(exactly = 1) { dao.insertAll(any()) }
    }

    @Test
    fun `getAlbums conserva los performers del API`() = runTest {
        coEvery { dao.getAll() } returns emptyList()
        coEvery { api.getAlbums() } returns listOf(ABBEY_ROAD_REMOTE)

        val album = repository.getAlbums().first()

        assertEquals(1, album.performers.size)
        assertEquals("The Beatles", album.performers.first().name)
    }

    @Test
    fun `getAlbums trunca releaseDate al anio`() = runTest {
        coEvery { dao.getAll() } returns emptyList()
        coEvery { api.getAlbums() } returns listOf(ABBEY_ROAD_REMOTE)

        val album = repository.getAlbums().first()

        assertEquals("1969", album.releaseDate)
    }

    @Test
    fun `refreshAlbums borra el cache y vuelve a llamar al API`() = runTest {
        coEvery { api.getAlbums() } returns listOf(ABBEY_ROAD_REMOTE)

        repository.refreshAlbums()

        coVerify(exactly = 1) { dao.deleteAll() }
        coVerify(exactly = 1) { api.getAlbums() }
        coVerify(exactly = 1) { dao.insertAll(any()) }
    }

    @Test
    fun `getAlbums devuelve lista vacia cuando el API devuelve vacio`() = runTest {
        coEvery { dao.getAll() } returns emptyList()
        coEvery { api.getAlbums() } returns emptyList()

        val result = repository.getAlbums()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `getAlbums propaga las excepciones del API`() = runTest {
        coEvery { dao.getAll() } returns emptyList()
        coEvery { api.getAlbums() } throws IOException("sin red")

        val thrown = assertThrows(IOException::class.java) {
            kotlinx.coroutines.runBlocking { repository.getAlbums() }
        }
        assertEquals("sin red", thrown.message)
    }

    // ─── HU08 — addTrackToAlbum ───────────────────────────────────────────────

    @Test
    fun `addTrackToAlbum llama al API, invalida la cache y devuelve Success`() = runTest {
        val request = CreateTrackRequest(name = "So What", duration = "9:22")
        val created = Track(id = 101, name = "So What", duration = "9:22")
        coEvery { api.addTrackToAlbum(7, request) } returns created

        val result = repository.addTrackToAlbum(7, request)

        assertTrue(result.isSuccess)
        assertEquals(101, result.getOrNull()?.id)
        coVerify(exactly = 1) { api.addTrackToAlbum(7, request) }
        coVerify(exactly = 1) { dao.deleteAll() }
    }

    @Test
    fun `addTrackToAlbum devuelve Failure cuando el API lanza excepcion`() = runTest {
        val request = CreateTrackRequest(name = "Track", duration = "3:00")
        coEvery { api.addTrackToAlbum(7, request) } throws IOException("sin red")

        val result = repository.addTrackToAlbum(7, request)

        assertTrue(result.isFailure)
        assertEquals("sin red", result.exceptionOrNull()?.message)
        // No debe invalidar cache si la red falló
        coVerify(exactly = 0) { dao.deleteAll() }
    }

    private companion object {
        val ABBEY_ROAD_REMOTE = Album(
            id = 1,
            name = "Abbey Road",
            cover = "https://img/cover.png",
            releaseDate = "1969-09-26T00:00:00.000Z",
            description = "Último álbum grabado por The Beatles.",
            genre = "Rock",
            recordLabel = "Apple Records",
            tracks = listOf(Track(1, "Come Together", "4:20")),
            performers = listOf(Performer(10, "The Beatles", "https://img/beatles.png", "Banda"))
        )

        val ABBEY_ROAD_ENTITY = AlbumEntity(
            id = 1,
            name = "Abbey Road",
            cover = "",
            releaseDate = "1969",
            description = "Cached.",
            genre = "Rock",
            recordLabel = "Apple",
            tracks = emptyList(),
            performers = listOf(Performer(10, "The Beatles", "", ""))
        )
    }
}
