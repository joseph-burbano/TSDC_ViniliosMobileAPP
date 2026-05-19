package com.uniandes.vinilos.ui.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.onAllNodesWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.compose.ui.test.onFirst
import com.uniandes.vinilos.model.Album
import com.uniandes.vinilos.model.Collector
import com.uniandes.vinilos.model.Performer
import com.uniandes.vinilos.model.UserRole
import com.uniandes.vinilos.repository.AlbumRepository
import com.uniandes.vinilos.repository.ArtistRepository
import com.uniandes.vinilos.repository.CollectorRepository
import com.uniandes.vinilos.ui.albums.AlbumViewModel
import com.uniandes.vinilos.ui.artists.ArtistViewModel
import com.uniandes.vinilos.ui.collectors.CollectorViewModel
import com.uniandes.vinilos.ui.theme.VinilosTheme
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeScreenInstrumentedTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val samplePerformer = Performer(
        id = 100,
        name = "Rubén Blades Bellido de Luna",
        image = "",
        description = "Cantante panameño",
        birthDate = "1948-07-16"
    )

    private val sampleAlbums = listOf(
        Album(
            id = 100, name = "Buscando América", cover = "",
            releaseDate = "1984", description = "desc",
            genre = "Salsa", recordLabel = "Elektra",
            performers = listOf(samplePerformer)
        ),
        Album(
            id = 101, name = "A Night at the Opera", cover = "",
            releaseDate = "1975", description = "desc",
            genre = "Rock", recordLabel = "EMI",
            performers = listOf(
                Performer(101, "Queen", "", "Banda británica", null, "1970-01-01")
            )
        )
    )

    private val samplePerformers = listOf(
        samplePerformer,
        Performer(101, "Queen", "", "Banda británica", null, "1970-01-01")
    )

    private val sampleCollectors = listOf(
        Collector(
            id = 100, name = "Manolo Bellon",
            telephone = "3502457896", email = "manollo@caracol.com.co",
            favoritePerformers = listOf(samplePerformer)
        ),
        Collector(
            id = 101, name = "Jaime Monsalve",
            telephone = "3012357936", email = "jmonsalve@rtvc.com.co",
            favoritePerformers = listOf(
                Performer(101, "Queen", "", "Banda británica", null, "1970-01-01")
            )
        )
    )

    private fun createViewModels(): Triple<AlbumViewModel, ArtistViewModel, CollectorViewModel> {
        val albumRepo = mockk<AlbumRepository>(relaxed = true)
        coEvery { albumRepo.getAlbums() } returns sampleAlbums

        val artistRepo = mockk<ArtistRepository>(relaxed = true)
        coEvery { artistRepo.getPerformers() } returns samplePerformers

        val collectorRepo = mockk<CollectorRepository>(relaxed = true)
        coEvery { collectorRepo.getCollectors() } returns sampleCollectors

        return Triple(
            AlbumViewModel(albumRepo),
            ArtistViewModel(artistRepo),
            CollectorViewModel(collectorRepo)
        )
    }

    // ─── Carga ────────────────────────────────────────────────────────────────

    @Test
    fun homeScreen_muestraHeader_cuandoCarga() {
        val (albumVm, artistVm, collectorVm) = createViewModels()

        composeTestRule.setContent {
            VinilosTheme {
                HomeScreen(
                    albumViewModel = albumVm,
                    artistViewModel = artistVm,
                    collectorViewModel = collectorVm,
                    userRole = UserRole.VISITOR
                )
            }
        }

        composeTestRule.onNodeWithText("RESUMEN").assertIsDisplayed()
        composeTestRule
            .onAllNodesWithText("Vinilos")
            .onFirst()
            .assertIsDisplayed()
    }

    @Test
    fun homeScreen_muestraSeccionAlbumes_cuandoCargaExitosa() {
        val (albumVm, artistVm, collectorVm) = createViewModels()

        composeTestRule.setContent {
            VinilosTheme {
                HomeScreen(
                    albumViewModel = albumVm,
                    artistViewModel = artistVm,
                    collectorViewModel = collectorVm,
                    userRole = UserRole.VISITOR
                )
            }
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("home_albums_row").assertIsDisplayed()
        composeTestRule.onNodeWithText("Buscando América").assertIsDisplayed()
        composeTestRule.onNodeWithText("A Night at the Opera").assertIsDisplayed()
    }

    @Test
    fun homeScreen_muestraSeccionArtistasConsultados_cuandoCargaExitosa() {
        val (albumVm, artistVm, collectorVm) = createViewModels()

        composeTestRule.setContent {
            VinilosTheme {
                HomeScreen(
                    albumViewModel = albumVm,
                    artistViewModel = artistVm,
                    collectorViewModel = collectorVm,
                    userRole = UserRole.VISITOR
                )
            }
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("home_consulted_artists_row").assertExists()
    }

    @Test
    fun homeScreen_muestraSeccionArtistasRecomendados_cuandoCargaExitosa() {
        val (albumVm, artistVm, collectorVm) = createViewModels()

        composeTestRule.setContent {
            VinilosTheme {
                HomeScreen(
                    albumViewModel = albumVm,
                    artistViewModel = artistVm,
                    collectorViewModel = collectorVm,
                    userRole = UserRole.VISITOR
                )
            }
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("home_recommended_artists_row").assertExists()
    }

    @Test
    fun homeScreen_muestraSeccionColeccionistas_cuandoCargaExitosa() {
        val (albumVm, artistVm, collectorVm) = createViewModels()

        composeTestRule.setContent {
            VinilosTheme {
                HomeScreen(
                    albumViewModel = albumVm,
                    artistViewModel = artistVm,
                    collectorViewModel = collectorVm,
                    userRole = UserRole.VISITOR
                )
            }
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("home_collectors_row").assertExists()
        composeTestRule.onNodeWithTag("home_collector_100").assertExists()
        composeTestRule.onNodeWithTag("home_collector_101").assertExists()
    }

    // ─── Navegación ───────────────────────────────────────────────────────────

    @Test
    fun homeScreen_clickEnAlbum_invocaCallbackConIdCorrecto() {
        val (albumVm, artistVm, collectorVm) = createViewModels()
        var clickedId: Int? = null

        composeTestRule.setContent {
            VinilosTheme {
                HomeScreen(
                    albumViewModel = albumVm,
                    artistViewModel = artistVm,
                    collectorViewModel = collectorVm,
                    userRole = UserRole.VISITOR,
                    onAlbumClick = { clickedId = it }
                )
            }
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("home_album_100").performClick()
        assert(clickedId == 100)
    }

    @Test
    fun homeScreen_coleccionistasExistenEnLaPantalla() {
        val (albumVm, artistVm, collectorVm) = createViewModels()

        composeTestRule.setContent {
            VinilosTheme {
                HomeScreen(
                    albumViewModel = albumVm,
                    artistViewModel = artistVm,
                    collectorViewModel = collectorVm,
                    userRole = UserRole.VISITOR
                )
            }
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("home_collector_100").assertExists()
        composeTestRule.onNodeWithTag("home_collector_101").assertExists()
    }

    @Test
    fun homeScreen_artistasExistenEnLaPantalla() {
        val (albumVm, artistVm, collectorVm) = createViewModels()

        composeTestRule.setContent {
            VinilosTheme {
                HomeScreen(
                    albumViewModel = albumVm,
                    artistViewModel = artistVm,
                    collectorViewModel = collectorVm,
                    userRole = UserRole.VISITOR
                )
            }
        }

        composeTestRule.waitForIdle()

        composeTestRule
            .onAllNodes(hasTestTag("home_artist_101"))
            .onFirst()
            .assertExists()
    }

    // ─── Nuevo: Rol COLLECTOR renderiza correctamente ─────────────────────────

    @Test
    fun homeScreen_renderizaCorrectamente_conRolCollector() {
        val (albumVm, artistVm, collectorVm) = createViewModels()

        composeTestRule.setContent {
            VinilosTheme {
                HomeScreen(
                    albumViewModel = albumVm,
                    artistViewModel = artistVm,
                    collectorViewModel = collectorVm,
                    userRole = UserRole.COLLECTOR
                )
            }
        }

        // El header siempre es visible independiente del rol
        composeTestRule.onNodeWithText("RESUMEN").assertIsDisplayed()
        composeTestRule
            .onAllNodesWithText("Vinilos")
            .onFirst()
            .assertIsDisplayed()

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("home_albums_row").assertIsDisplayed()
    }
}
