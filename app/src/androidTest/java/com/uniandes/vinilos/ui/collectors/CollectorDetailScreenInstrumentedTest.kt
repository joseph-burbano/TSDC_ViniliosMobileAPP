package com.uniandes.vinilos.ui.collectors

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.uniandes.vinilos.model.Album
import com.uniandes.vinilos.model.Collector
import com.uniandes.vinilos.model.CollectorAlbum
import com.uniandes.vinilos.model.UserRole
import com.uniandes.vinilos.ui.theme.VinilosTheme
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CollectorDetailScreenInstrumentedTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val sampleAlbum = Album(
        id = 10, name = "Kind of Blue", cover = "", releaseDate = "1959",
        description = "Jazz masterpiece", genre = "Jazz", recordLabel = "Columbia"
    )
    private val sampleCollector = Collector(
        id = 1,
        name = "Alejandro Vance",
        telephone = "3001234567",
        email = "alejandro@vinilos.com",
        description = "Preserving the warmth of analog signals since 1998.",
        image = "",
        collectorAlbums = listOf(
            CollectorAlbum(id = 1, price = 100, status = "NM", album = sampleAlbum)
        )
    )

    private fun mockViewModel(
        isLoading: Boolean = false,
        collector: Collector? = sampleCollector
    ): CollectorViewModel {
        val vm = mockk<CollectorViewModel>(relaxed = true)
        every { vm.isLoading } returns MutableStateFlow(isLoading)
        every { vm.collectors } returns MutableStateFlow(if (collector != null) listOf(collector) else emptyList())
        every { vm.findById(any()) } returns collector
        // loadCollector es suspend — se intercepta con coEvery para evitar
        // que el LaunchedEffect lance una corrutina real sobre el mock
        coEvery { vm.loadCollector(any()) } returns Unit
        return vm
    }

    // ─── HU06 - T1: Spinner mientras isLoading = true ────────────────────────

    @Test
    fun detailScreen_muestraIndicadorDeCarga_cuandoIsLoadingEsTrue() {
        val vm = mockViewModel(isLoading = true)

        composeTestRule.setContent {
            VinilosTheme {
                CollectorDetailScreen(
                    collectorId = 1,
                    viewModel = vm,
                    userRole = UserRole.VISITOR
                )
            }
        }

        composeTestRule
            .onNodeWithTag(CollectorDetailTestTags.LOADING)
            .assertIsDisplayed()
    }

    // ─── HU06 - T2: Spinner cuando collector es null ──────────────────────────

    @Test
    fun detailScreen_muestraIndicadorDeCarga_cuandoCollectorEsNull() {
        val vm = mockViewModel(isLoading = false, collector = null)

        composeTestRule.setContent {
            VinilosTheme {
                CollectorDetailScreen(
                    collectorId = 999,
                    viewModel = vm,
                    userRole = UserRole.VISITOR
                )
            }
        }

        composeTestRule
            .onNodeWithTag(CollectorDetailTestTags.LOADING)
            .assertIsDisplayed()
    }

    // ─── HU06 - T3: Nombre del coleccionista visible tras carga ───────────────

    @Test
    fun detailScreen_muestraNombreDelCollector_cuandoCargaExitosa() {
        val vm = mockViewModel()

        composeTestRule.setContent {
            VinilosTheme {
                CollectorDetailScreen(
                    collectorId = sampleCollector.id,
                    viewModel = vm,
                    userRole = UserRole.VISITOR
                )
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule
                .onAllNodes(hasTestTag(CollectorDetailTestTags.SCREEN))
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule
            .onNodeWithTag(CollectorDetailTestTags.NAME)
            .assertIsDisplayed()
    }

    // ─── HU06 - T4: Sección Stats visible con álbumes ─────────────────────────

    @Test
    fun detailScreen_muestraSeccionStats_cuandoCollectorTieneAlbumes() {
        val vm = mockViewModel()

        composeTestRule.setContent {
            VinilosTheme {
                CollectorDetailScreen(
                    collectorId = sampleCollector.id,
                    viewModel = vm,
                    userRole = UserRole.VISITOR
                )
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule
                .onAllNodes(hasTestTag(CollectorDetailTestTags.SCREEN))
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule
            .onNodeWithTag(CollectorDetailTestTags.STATS)
            .assertIsDisplayed()
    }

    // ─── HU06 - T5: Sección Vault visible con álbumes ─────────────────────────

    @Test
    fun detailScreen_muestraSeccionVault_cuandoCollectorTieneAlbumes() {
        val vm = mockViewModel()

        composeTestRule.setContent {
            VinilosTheme {
                CollectorDetailScreen(
                    collectorId = sampleCollector.id,
                    viewModel = vm,
                    userRole = UserRole.VISITOR
                )
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule
                .onAllNodes(hasTestTag(CollectorDetailTestTags.SCREEN))
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule
            .onNodeWithTag(CollectorDetailTestTags.VAULT)
            .assertExists()
    }

    // ─── HU06 - T6: Botón atrás invoca callback ───────────────────────────────

    @Test
    fun detailScreen_botonAtras_invocaCallbackOnBack() {
        var backInvoked = false
        val vm = mockViewModel()

        composeTestRule.setContent {
            VinilosTheme {
                CollectorDetailScreen(
                    collectorId = sampleCollector.id,
                    viewModel = vm,
                    onBack = { backInvoked = true },
                    userRole = UserRole.VISITOR
                )
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule
                .onAllNodes(hasTestTag(CollectorDetailTestTags.BACK))
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithTag(CollectorDetailTestTags.BACK).performClick()
        assertTrue(backInvoked)
    }

    // ─── HU06 - T7 (nuevo): Rol COLLECTOR no rompe la pantalla ───────────────

    @Test
    fun detailScreen_renderizaCorrectamente_conRolCollector() {
        val vm = mockViewModel()

        composeTestRule.setContent {
            VinilosTheme {
                CollectorDetailScreen(
                    collectorId = sampleCollector.id,
                    viewModel = vm,
                    userRole = UserRole.COLLECTOR
                )
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule
                .onAllNodes(hasTestTag(CollectorDetailTestTags.SCREEN))
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule
            .onNodeWithTag(CollectorDetailTestTags.NAME)
            .assertIsDisplayed()
    }

    // ─── HU15 - T1: FAB visible para rol COLLECTOR ────────────────────────────

    @Test
    fun detailScreen_fabAgregarArtista_visibleParaRolCollector() {
        val vm = mockViewModel()

        composeTestRule.setContent {
            VinilosTheme {
                CollectorDetailScreen(
                    collectorId = sampleCollector.id,
                    viewModel = vm,
                    userRole = UserRole.COLLECTOR
                )
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule
                .onAllNodes(hasTestTag(CollectorDetailTestTags.SCREEN))
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule
            .onNodeWithContentDescription("Agregar artista favorito")
            .assertExists()
    }

    // ─── HU15 - T2: FAB oculto para rol VISITOR ───────────────────────────────

    @Test
    fun detailScreen_fabAgregarArtista_ocultoPararRolVisitor() {
        val vm = mockViewModel()

        composeTestRule.setContent {
            VinilosTheme {
                CollectorDetailScreen(
                    collectorId = sampleCollector.id,
                    viewModel = vm,
                    userRole = UserRole.VISITOR
                )
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule
                .onAllNodes(hasTestTag(CollectorDetailTestTags.SCREEN))
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule
            .onNodeWithContentDescription("Agregar artista favorito")
            .assertDoesNotExist()
    }

    // ─── HU15 - T3: Click en FAB invoca callback onAddFavoriteArtist ──────────

    @Test
    fun detailScreen_fabAgregarArtista_alClickInvocaCallback() {
        var callbackInvoked = false
        val vm = mockViewModel()

        composeTestRule.setContent {
            VinilosTheme {
                CollectorDetailScreen(
                    collectorId = sampleCollector.id,
                    viewModel = vm,
                    userRole = UserRole.COLLECTOR,
                    onAddFavoriteArtist = { callbackInvoked = true }
                )
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule
                .onAllNodes(hasTestTag(CollectorDetailTestTags.SCREEN))
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule
            .onNodeWithContentDescription("Agregar artista favorito")
            .performClick()

        assertTrue(callbackInvoked)
    }

    // ─── HU15 - T4: Sección artistas favoritos muestra los nombres ────────────

    @Test
    fun detailScreen_seccionArtistasFavoritos_muestraNombreDelArtista() {
        val performer = com.uniandes.vinilos.model.Performer(
            id = 99, name = "Rubén Blades", image = "",
            description = "Cantante panameño", birthDate = "1948-07-16"
        )
        val collectorWithFavorite = sampleCollector.copy(favoritePerformers = listOf(performer))
        val vm = mockViewModel(collector = collectorWithFavorite)

        composeTestRule.setContent {
            VinilosTheme {
                CollectorDetailScreen(
                    collectorId = collectorWithFavorite.id,
                    viewModel = vm,
                    userRole = UserRole.VISITOR
                )
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule
                .onAllNodes(hasTestTag(CollectorDetailTestTags.SCREEN))
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule
            .onNodeWithText("Rubén Blades")
            .assertExists()
    }

    // ─── HU15 - T5: Botón GESTIONAR visible para COLLECTOR ───────────────────

    @Test
    fun detailScreen_botonGestionar_visibleParaRolCollector() {
        val vm = mockViewModel()

        composeTestRule.setContent {
            VinilosTheme {
                CollectorDetailScreen(
                    collectorId = sampleCollector.id,
                    viewModel = vm,
                    userRole = UserRole.COLLECTOR
                )
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule
                .onAllNodes(hasTestTag(CollectorDetailTestTags.SCREEN))
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule
            .onNodeWithText("GESTIONAR")
            .assertExists()
    }
}
