package com.uniandes.vinilos.ui.collectors

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.uniandes.vinilos.model.Collector
import com.uniandes.vinilos.model.Performer
import com.uniandes.vinilos.model.UserRole
import com.uniandes.vinilos.ui.theme.VinilosTheme
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FavoritePerformersScreenInstrumentedTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val sampleMusician = Performer(
        id = 1, name = "Miles Davis", image = "",
        description = "Jazz trumpeter", birthDate = "1926-05-26", type = "musician"
    )
    private val sampleBand = Performer(
        id = 2, name = "The Beatles", image = "",
        description = "Rock band", creationDate = "1960-01-01", type = "band"
    )
    private val sampleCollector = Collector(
        id = 10, name = "Alejandro Vance",
        telephone = "3001234567", email = "alejandro@vinilos.com",
        favoritePerformers = listOf(sampleMusician)
    )

    private fun mockViewModel(
        allPerformers: List<Performer> = listOf(sampleMusician, sampleBand),
        favoriteIds: Set<Int> = setOf(sampleMusician.id),
        isLoading: Boolean = false,
        isTogglingId: Int? = null,
        error: String? = null
    ): FavoritePerformersViewModel {
        val vm = mockk<FavoritePerformersViewModel>(relaxed = true)
        every { vm.allPerformers } returns MutableStateFlow(allPerformers)
        every { vm.favoriteIds } returns MutableStateFlow(favoriteIds)
        every { vm.isLoading } returns MutableStateFlow(isLoading)
        every { vm.isTogglingId } returns MutableStateFlow(isTogglingId)
        every { vm.error } returns MutableStateFlow(error)
        return vm
    }

    // ─── HU15 - T1: Spinner mientras isLoading = true ────────────────────────

    @Test
    fun screen_muestraIndicadorDeCarga_cuandoIsLoadingEsTrue() {
        val vm = mockViewModel(isLoading = true, allPerformers = emptyList())

        composeTestRule.setContent {
            VinilosTheme {
                FavoritePerformersScreen(
                    collector = sampleCollector,
                    viewModel = vm,
                    userRole = UserRole.COLLECTOR
                )
            }
        }

        composeTestRule
            .onNodeWithTag(FavoritePerformersTestTags.LOADING)
            .assertIsDisplayed()
    }

    // ─── HU15 - T2: Mensaje cuando collector es null ──────────────────────────

    @Test
    fun screen_muestraMensaje_cuandoCollectorEsNull() {
        val vm = mockViewModel()

        composeTestRule.setContent {
            VinilosTheme {
                FavoritePerformersScreen(
                    collector = null,
                    viewModel = vm,
                    userRole = UserRole.COLLECTOR
                )
            }
        }

        composeTestRule
            .onNodeWithTag(FavoritePerformersTestTags.EMPTY)
            .assertIsDisplayed()
    }

    // ─── HU15 - T3: Mensaje cuando lista de artistas está vacía ──────────────

    @Test
    fun screen_muestraMensaje_cuandoNoHayArtistasDisponibles() {
        val vm = mockViewModel(allPerformers = emptyList(), isLoading = false)

        composeTestRule.setContent {
            VinilosTheme {
                FavoritePerformersScreen(
                    collector = sampleCollector,
                    viewModel = vm,
                    userRole = UserRole.COLLECTOR
                )
            }
        }

        composeTestRule
            .onNodeWithTag(FavoritePerformersTestTags.EMPTY)
            .assertIsDisplayed()
    }

    // ─── HU15 - T4: Lista de artistas visible con datos ───────────────────────

    @Test
    fun screen_muestraListaDeArtistas_cuandoHayDatos() {
        val vm = mockViewModel()

        composeTestRule.setContent {
            VinilosTheme {
                FavoritePerformersScreen(
                    collector = sampleCollector,
                    viewModel = vm,
                    userRole = UserRole.COLLECTOR
                )
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule
                .onAllNodes(hasTestTag(FavoritePerformersTestTags.LIST))
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule
            .onNodeWithTag(FavoritePerformersTestTags.LIST)
            .assertIsDisplayed()
    }

    // ─── HU15 - T5: Item de artista visible en la lista ───────────────────────

    @Test
    fun screen_muestraItemDeArtista_paraMusico() {
        val vm = mockViewModel()

        composeTestRule.setContent {
            VinilosTheme {
                FavoritePerformersScreen(
                    collector = sampleCollector,
                    viewModel = vm,
                    userRole = UserRole.COLLECTOR
                )
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule
                .onAllNodes(hasTestTag("${FavoritePerformersTestTags.ITEM_PREFIX}${sampleMusician.id}"))
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule
            .onNodeWithTag("${FavoritePerformersTestTags.ITEM_PREFIX}${sampleMusician.id}")
            .assertExists()
    }

    // ─── HU15 - T6: Botón toggle visible para cada artista ───────────────────

    @Test
    fun screen_muestraBotonToggle_paraArtista() {
        val vm = mockViewModel()

        composeTestRule.setContent {
            VinilosTheme {
                FavoritePerformersScreen(
                    collector = sampleCollector,
                    viewModel = vm,
                    userRole = UserRole.COLLECTOR
                )
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule
                .onAllNodes(hasTestTag("${FavoritePerformersTestTags.TOGGLE_PREFIX}${sampleMusician.id}"))
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule
            .onNodeWithTag("${FavoritePerformersTestTags.TOGGLE_PREFIX}${sampleMusician.id}")
            .assertExists()
    }

    // ─── HU15 - T7: Click en toggle llama toggleFavorite del ViewModel ────────

    @Test
    fun screen_alClickEnToggle_llamaToggleFavoriteEnViewModel() {
        val vm = mockViewModel()

        composeTestRule.setContent {
            VinilosTheme {
                FavoritePerformersScreen(
                    collector = sampleCollector,
                    viewModel = vm,
                    userRole = UserRole.COLLECTOR
                )
            }
        }

        // waitForIdle() avanza el reloj de Compose de forma controlada (frame a frame),
        // evitando que el bucle de waitUntil sature la cola de buffers del GPU
        // (dequeueBuffer ETIMEDOUT) y cause un SIGSEGV en el RenderThread al hacer teardown.
        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithTag("${FavoritePerformersTestTags.TOGGLE_PREFIX}${sampleMusician.id}")
            .assertExists()
            .performClick()

        // Deja que el reloj de Compose procese todas las animaciones pendientes
        // (p.ej. el ripple del IconButton) antes de que el framework destruya la actividad.
        composeTestRule.waitForIdle()

        verify(exactly = 1) { vm.toggleFavorite(sampleCollector.id, sampleMusician) }
    }

    // ─── HU15 - T8: Botón Volver invoca callback onBack ──────────────────────

    @Test
    fun screen_botonAtras_invocaCallbackOnBack() {
        var backInvoked = false
        val vm = mockViewModel()

        composeTestRule.setContent {
            VinilosTheme {
                FavoritePerformersScreen(
                    collector = sampleCollector,
                    viewModel = vm,
                    onBack = { backInvoked = true },
                    userRole = UserRole.COLLECTOR
                )
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule
                .onAllNodes(hasTestTag(FavoritePerformersTestTags.LIST))
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithTag("top_bar_back_button").performClick()
        assertTrue(backInvoked)
    }
}
