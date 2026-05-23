package com.uniandes.vinilos.ui.artists

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.uniandes.vinilos.model.Album
import com.uniandes.vinilos.model.Performer
import com.uniandes.vinilos.model.UserRole
import com.uniandes.vinilos.ui.theme.VinilosTheme
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ArtistDetailScreenInstrumentedTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val sampleAlbum = Album(
        id = 10, name = "Kind of Blue", cover = "", releaseDate = "1959-08-17",
        description = "Jazz masterpiece", genre = "Jazz", recordLabel = "Columbia"
    )
    private val samplePerformer = Performer(
        id = 1, name = "Miles Davis", image = "", description = "American jazz trumpeter.",
        birthDate = "1926-05-26", albums = listOf(sampleAlbum)
    )

    private fun mockViewModel(
        isLoading: Boolean = false,
        performer: Performer? = samplePerformer
    ): ArtistViewModel {
        val vm = mockk<ArtistViewModel>(relaxed = true)
        every { vm.isLoading } returns MutableStateFlow(isLoading)
        every { vm.findById(any()) } returns performer
        return vm
    }

    // ─── HU04 - T1: Spinner mientras isLoading = true ────────────────────────

    @Test
    fun detailScreen_muestraIndicadorDeCarga_cuandoIsLoadingEsTrue() {
        val vm = mockViewModel(isLoading = true)

        composeTestRule.setContent {
            VinilosTheme {
                ArtistDetailScreen(
                    artistId = 1,
                    viewModel = vm,
                    userRole = UserRole.VISITOR
                )
            }
        }

        composeTestRule
            .onNodeWithTag(ArtistDetailTestTags.LOADING)
            .assertIsDisplayed()
    }

    // ─── HU04 - T2: Spinner cuando performer es null ──────────────────────────

    @Test
    fun detailScreen_muestraIndicadorDeCarga_cuandoPerformerEsNull() {
        val vm = mockViewModel(isLoading = false, performer = null)

        composeTestRule.setContent {
            VinilosTheme {
                ArtistDetailScreen(
                    artistId = 999,
                    viewModel = vm,
                    userRole = UserRole.VISITOR
                )
            }
        }

        composeTestRule
            .onNodeWithTag(ArtistDetailTestTags.LOADING)
            .assertIsDisplayed()
    }

    // ─── HU04 - T3: Nombre del artista visible tras carga ─────────────────────

    @Test
    fun detailScreen_muestraNombreDelArtista_cuandoCargaExitosa() {
        val vm = mockViewModel()

        composeTestRule.setContent {
            VinilosTheme {
                ArtistDetailScreen(
                    artistId = samplePerformer.id,
                    viewModel = vm,
                    userRole = UserRole.VISITOR
                )
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule
                .onAllNodes(hasTestTag(ArtistDetailTestTags.SCREEN))
                .fetchSemanticsNodes().isNotEmpty()
        }

        // El nombre aparece dos veces en pantalla (hero + descripción),
        // usamos el testTag para apuntar específicamente al nodo del hero
        composeTestRule
            .onNodeWithTag(ArtistDetailTestTags.NAME)
            .assertIsDisplayed()
    }

    // ─── HU04 - T4: Sección discografía visible con álbumes ──────────────────

    @Test
    fun detailScreen_muestraSectionDiscografia_cuandoArtistaTieneAlbumes() {
        val vm = mockViewModel()

        composeTestRule.setContent {
            VinilosTheme {
                ArtistDetailScreen(
                    artistId = samplePerformer.id,
                    viewModel = vm,
                    userRole = UserRole.VISITOR
                )
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule
                .onAllNodes(hasTestTag(ArtistDetailTestTags.SCREEN))
                .fetchSemanticsNodes().isNotEmpty()
        }

        // La sección de álbumes puede estar fuera del viewport inicial
        // (la pantalla es scrollable), assertExists verifica que esté en la composición
        composeTestRule
            .onNodeWithTag(ArtistDetailTestTags.ALBUMS)
            .assertExists()

        composeTestRule
            .onNodeWithText("Kind of Blue")
            .assertExists()
    }

    // ─── HU04 - T5: Botón atrás invoca callback ───────────────────────────────

    @Test
    fun detailScreen_botonAtras_invocaCallbackOnBack() {
        var backInvoked = false
        val vm = mockViewModel()

        composeTestRule.setContent {
            VinilosTheme {
                ArtistDetailScreen(
                    artistId = samplePerformer.id,
                    viewModel = vm,
                    onBack = { backInvoked = true },
                    userRole = UserRole.VISITOR
                )
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule
                .onAllNodes(hasTestTag(ArtistDetailTestTags.BACK))
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithTag(ArtistDetailTestTags.BACK).performClick()
        assertTrue(backInvoked)
    }

    // ─── HU04 - T6 (nuevo): Rol COLLECTOR no rompe la pantalla ───────────────

    @Test
    fun detailScreen_renderizaCorrectamente_conRolCollector() {
        val vm = mockViewModel()

        composeTestRule.setContent {
            VinilosTheme {
                ArtistDetailScreen(
                    artistId = samplePerformer.id,
                    viewModel = vm,
                    userRole = UserRole.COLLECTOR
                )
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule
                .onAllNodes(hasTestTag(ArtistDetailTestTags.SCREEN))
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule
            .onNodeWithTag(ArtistDetailTestTags.NAME)
            .assertIsDisplayed()
    }

    // ─── HU13 - T1: Sección GALARDONES visible para COLLECTOR ────────────────

    @Test
    fun detailScreen_muestraSeccionGalardones_paraCollector() {
        val vm = mockViewModel()

        composeTestRule.setContent {
            VinilosTheme {
                ArtistDetailScreen(
                    artistId = samplePerformer.id,
                    viewModel = vm,
                    userRole = UserRole.COLLECTOR
                )
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule
                .onAllNodes(hasTestTag(ArtistDetailTestTags.SCREEN))
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule
            .onNodeWithTag(ArtistDetailTestTags.PRIZES_SECTION)
            .assertExists()
        composeTestRule
            .onNodeWithTag(ArtistDetailTestTags.PRIZES_CTA)
            .assertExists()
    }

    // ─── HU13 - T2: Sección GALARDONES oculta para VISITOR ───────────────────

    @Test
    fun detailScreen_ocultaSeccionGalardones_paraVisitor() {
        val vm = mockViewModel()

        composeTestRule.setContent {
            VinilosTheme {
                ArtistDetailScreen(
                    artistId = samplePerformer.id,
                    viewModel = vm,
                    userRole = UserRole.VISITOR
                )
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule
                .onAllNodes(hasTestTag(ArtistDetailTestTags.SCREEN))
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule
            .onNodeWithTag(ArtistDetailTestTags.PRIZES_SECTION)
            .assertDoesNotExist()
    }

    // ─── HU13 - T3: CTA Asociar premio invoca callback ───────────────────────

    @Test
    fun detailScreen_ctaAsociarPremio_invocaCallback() {
        var navigatedToArtistId: Int? = null
        val vm = mockViewModel()

        composeTestRule.setContent {
            VinilosTheme {
                ArtistDetailScreen(
                    artistId = samplePerformer.id,
                    viewModel = vm,
                    onAssociatePrize = { navigatedToArtistId = it },
                    userRole = UserRole.COLLECTOR
                )
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule
                .onAllNodes(hasTestTag(ArtistDetailTestTags.PRIZES_CTA))
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithTag(ArtistDetailTestTags.PRIZES_CTA)
            .performScrollTo()
            .performClick()
        assertTrue(navigatedToArtistId == samplePerformer.id)
    }
}
