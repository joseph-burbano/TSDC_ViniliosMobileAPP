package com.uniandes.vinilos.ui.albums

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.uniandes.vinilos.model.Track
import com.uniandes.vinilos.repository.AlbumRepository
import com.uniandes.vinilos.ui.theme.VinilosTheme
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.delay
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AddTrackScreenInstrumentedTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val albumId = 7
    private val createdTrack = Track(id = 101, name = "So What", duration = "9:22")

    private fun viewModelWith(
        repoBlock: AlbumRepository.() -> Unit = {}
    ): CreateTrackViewModel {
        val repo = mockk<AlbumRepository>(relaxed = true).apply(repoBlock)
        return CreateTrackViewModel(repo)
    }

    private fun setScreen(
        viewModel: CreateTrackViewModel,
        albumName: String = "Kind of Blue",
        onSuccess: () -> Unit = {},
        onDiscard: () -> Unit = {}
    ) {
        composeTestRule.setContent {
            VinilosTheme {
                AddTrackScreen(
                    albumId = albumId,
                    albumName = albumName,
                    viewModel = viewModel,
                    onSuccess = onSuccess,
                    onDiscard = onDiscard
                )
            }
        }
    }

    // ─── T1: La pantalla renderiza los elementos principales ─────────────────

    @Test
    fun addTrackScreen_muestraElementosPrincipales() {
        setScreen(viewModelWith())

        composeTestRule.onNodeWithTag(AddTrackTestTags.SCREEN).assertIsDisplayed()
        composeTestRule.onNodeWithText("Asociar canción").assertIsDisplayed()
        composeTestRule.onNodeWithText("TRACK ASSOCIATION").assertIsDisplayed()
        composeTestRule.onNodeWithTag(AddTrackTestTags.INPUT_NAME).assertExists()
        composeTestRule.onNodeWithTag(AddTrackTestTags.INPUT_DURATION).assertExists()
        composeTestRule.onNodeWithTag(AddTrackTestTags.BTN_SUBMIT).assertExists()
        composeTestRule.onNodeWithTag(AddTrackTestTags.BTN_DISCARD).assertExists()
    }

    // ─── T2: La pantalla muestra el nombre del álbum recibido como contexto ──

    @Test
    fun addTrackScreen_muestraNombreDelAlbum() {
        setScreen(viewModelWith(), albumName = "Abbey Road")

        composeTestRule
            .onNode(hasTestTag(AddTrackTestTags.SCREEN))
            .assertExists()
        composeTestRule.onNodeWithText(
            "Agrega una canción al álbum \"Abbey Road\". Documenta el orden, la duración exacta y mantén el archivo curado.",
            substring = false
        ).assertExists()
    }

    // ─── T3: Submit vacío muestra errores de validación ──────────────────────

    @Test
    fun addTrackScreen_muestraErroresDeValidacion_conFormularioVacio() {
        setScreen(viewModelWith())

        composeTestRule.onNodeWithTag(AddTrackTestTags.BTN_SUBMIT).performScrollTo().performClick()

        composeTestRule.waitUntil(timeoutMillis = 3_000) {
            composeTestRule
                .onAllNodes(hasTestTag("${AddTrackTestTags.INPUT_NAME}_error"))
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithText("El nombre de la canción es obligatorio").assertExists()
        composeTestRule.onNodeWithText("La duración es obligatoria").assertExists()
    }

    // ─── T4: Formato de duración inválido muestra el error correspondiente ───

    @Test
    fun addTrackScreen_muestraErrorDeFormato_cuandoDuracionInvalida() {
        setScreen(viewModelWith())

        composeTestRule.onNodeWithTag(AddTrackTestTags.INPUT_NAME)
            .performScrollTo().performTextInput("So What")
        composeTestRule.onNodeWithTag(AddTrackTestTags.INPUT_DURATION)
            .performScrollTo().performTextInput("nueve minutos")

        composeTestRule.onNodeWithTag(AddTrackTestTags.BTN_SUBMIT).performScrollTo().performClick()

        composeTestRule.waitUntil(timeoutMillis = 3_000) {
            composeTestRule
                .onAllNodes(hasTestTag("${AddTrackTestTags.INPUT_DURATION}_error"))
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithText("Formato esperado mm:ss (ej. 3:45)").assertExists()
    }

    // ─── T5: Flujo exitoso invoca el callback onSuccess ──────────────────────

    @Test
    fun addTrackScreen_submitExitoso_invocaOnSuccess() {
        var successInvoked = false
        val vm = viewModelWith {
            coEvery { addTrackToAlbum(albumId, any()) } returns Result.success(createdTrack)
        }
        setScreen(vm, onSuccess = { successInvoked = true })

        composeTestRule.onNodeWithTag(AddTrackTestTags.INPUT_NAME)
            .performScrollTo().performTextInput("So What")
        composeTestRule.onNodeWithTag(AddTrackTestTags.INPUT_DURATION)
            .performScrollTo().performTextInput("9:22")

        composeTestRule.onNodeWithTag(AddTrackTestTags.BTN_SUBMIT).performScrollTo().performClick()

        composeTestRule.waitUntil(timeoutMillis = 5_000) { successInvoked }

        assertTrue(successInvoked)
    }

    // ─── T6: Botón descartar invoca onDiscard ────────────────────────────────

    @Test
    fun addTrackScreen_botonDescartar_invocaOnDiscard() {
        var discardInvoked = false
        setScreen(viewModelWith(), onDiscard = { discardInvoked = true })

        composeTestRule.onNodeWithTag(AddTrackTestTags.BTN_DISCARD).performScrollTo().performClick()

        assertTrue(discardInvoked)
    }

    // ─── T7: Error del repositorio muestra mensaje global ────────────────────

    @Test
    fun addTrackScreen_muestraMensajeDeError_cuandoRepositorioFalla() {
        val vm = viewModelWith {
            coEvery { addTrackToAlbum(albumId, any()) } returns Result.failure(
                java.io.IOException("sin red")
            )
        }
        setScreen(vm)

        composeTestRule.onNodeWithTag(AddTrackTestTags.INPUT_NAME)
            .performScrollTo().performTextInput("So What")
        composeTestRule.onNodeWithTag(AddTrackTestTags.INPUT_DURATION)
            .performScrollTo().performTextInput("9:22")

        composeTestRule.onNodeWithTag(AddTrackTestTags.BTN_SUBMIT).performScrollTo().performClick()

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule
                .onAllNodes(hasTestTag(AddTrackTestTags.ERROR_GLOBAL))
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithTag(AddTrackTestTags.ERROR_GLOBAL).assertExists()
    }

    // ─── T8: Botón deshabilitado durante carga ───────────────────────────────

    @Test
    fun addTrackScreen_botonDeshabilitado_duranteCarga() {
        val vm = viewModelWith {
            coEvery { addTrackToAlbum(albumId, any()) } coAnswers {
                delay(10_000)
                Result.success(createdTrack)
            }
        }
        setScreen(vm)

        composeTestRule.onNodeWithTag(AddTrackTestTags.INPUT_NAME)
            .performScrollTo().performTextInput("So What")
        composeTestRule.onNodeWithTag(AddTrackTestTags.INPUT_DURATION)
            .performScrollTo().performTextInput("9:22")

        composeTestRule.onNodeWithTag(AddTrackTestTags.BTN_SUBMIT).performScrollTo().performClick()
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.mainClock.advanceTimeBy(100)

        composeTestRule.onNodeWithTag(AddTrackTestTags.BTN_SUBMIT).assertIsNotEnabled()
    }
}
