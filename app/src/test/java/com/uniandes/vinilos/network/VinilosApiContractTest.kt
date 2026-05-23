package com.uniandes.vinilos.network

import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class VinilosApiContractTest {

    private lateinit var server: MockWebServer
    private lateinit var api: VinilosApi

    @Before
    fun start() {
        server = MockWebServer().apply { start() }
        api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(VinilosApi::class.java)
    }

    @After
    fun stop() {
        server.shutdown()
    }

    @Test
    fun `getAlbums hace GET a la ruta albums`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody("[]"))

        api.getAlbums()

        val request = server.takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/albums", request.path)
    }

    @Test
    fun `getAlbums deserializa correctamente la respuesta del backend`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody(SAMPLE_ALBUMS_JSON))

        val albums = api.getAlbums()

        assertEquals(1, albums.size)
        val album = albums.first()
        assertEquals(1, album.id)
        assertEquals("Buscando América", album.name)
        assertEquals("Rock", album.genre)
        assertEquals("Elektra", album.recordLabel)
        assertEquals(1, album.performers.size)
        assertEquals("Rubén Blades", album.performers.first().name)
        assertEquals(2, album.tracks.size)
        assertEquals("Decisiones", album.tracks.first().name)
    }

    @Test
    fun `getAlbums tolera campos opcionales nulos del backend`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody(ALBUM_WITH_NULLS_JSON))

        val albums = api.getAlbums()

        assertEquals(1, albums.size)
        val album = albums.first()
        assertEquals("Sin metadatos", album.name)
        assertTrue(album.tracks.isEmpty())
        assertTrue(album.performers.isEmpty())
    }

    // ─── HU-13: Contrato de premios ───────────────────────────────────────────

    @Test
    fun `getPrizes hace GET a la ruta prizes`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody("[]"))

        api.getPrizes()

        val request = server.takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/prizes", request.path)
    }

    @Test
    fun `getPrizes deserializa la lista`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody(SAMPLE_PRIZES_JSON))

        val prizes = api.getPrizes()

        assertEquals(2, prizes.size)
        assertEquals("Grammy", prizes[0].name)
        assertEquals("NARAS", prizes[0].organization)
    }

    @Test
    fun `createPrize hace POST con el body y devuelve el premio creado`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody(CREATED_PRIZE_JSON))

        val created = api.createPrize(
            PrizeCreateBody(name = "Mercury", description = "UK", organization = "BPI")
        )

        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/prizes", request.path)
        val body = request.body.readUtf8()
        assertTrue(body.contains("\"name\":\"Mercury\""))
        assertTrue(body.contains("\"organization\":\"BPI\""))
        assertEquals(99, created.id)
        assertEquals("Mercury", created.name)
    }

    @Test
    fun `associatePrizeToMusician hace POST a la ruta musicians y manda la fecha`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody(ASSOCIATION_RESPONSE_JSON))

        api.associatePrizeToMusician(7, 22, PerformerPrizeBody(premiationDate = "2024-05-01"))

        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/prizes/7/musicians/22", request.path)
        assertTrue(request.body.readUtf8().contains("\"premiationDate\":\"2024-05-01\""))
    }

    @Test
    fun `associatePrizeToBand hace POST a la ruta bands`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody(ASSOCIATION_RESPONSE_JSON))

        api.associatePrizeToBand(7, 33, PerformerPrizeBody(premiationDate = "1999-12-31"))

        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/prizes/7/bands/33", request.path)
    }

    private companion object {
        const val SAMPLE_ALBUMS_JSON = """
        [
          {
            "id": 1,
            "name": "Buscando América",
            "cover": "https://image",
            "releaseDate": "1984-08-01T00:00:00.000Z",
            "description": "Álbum del cantautor Rubén Blades",
            "genre": "Rock",
            "recordLabel": "Elektra",
            "performers": [
              { "id": 100, "name": "Rubén Blades", "image": "img", "description": "Cantautor" }
            ],
            "tracks": [
              { "id": 1, "name": "Decisiones", "duration": "5:05" },
              { "id": 2, "name": "Desapariciones", "duration": "6:25" }
            ]
          }
        ]
        """

        const val ALBUM_WITH_NULLS_JSON = """
        [
          {
            "id": 99,
            "name": "Sin metadatos",
            "cover": null,
            "releaseDate": null,
            "description": null,
            "genre": null,
            "recordLabel": null,
            "performers": [],
            "tracks": []
          }
        ]
        """

        const val SAMPLE_PRIZES_JSON = """
        [
          { "id": 1, "name": "Grammy", "description": "Music award", "organization": "NARAS" },
          { "id": 2, "name": "Latin Grammy", "description": "Latin music award", "organization": "LARAS" }
        ]
        """

        const val CREATED_PRIZE_JSON = """
        { "id": 99, "name": "Mercury", "description": "UK", "organization": "BPI" }
        """

        const val ASSOCIATION_RESPONSE_JSON = """
        { "id": 5, "premiationDate": "2024-05-01" }
        """
    }
}
