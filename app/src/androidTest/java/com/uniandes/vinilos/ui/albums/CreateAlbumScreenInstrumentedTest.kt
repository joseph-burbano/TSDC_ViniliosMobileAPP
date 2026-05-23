package com.uniandes.vinilos.ui.albums

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.SemanticsMatcher
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
import com.uniandes.vinilos.model.Album
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
class CreateAlbumScreenInstrumentedTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val createdAlbum = Album(
        id = 99,
        name = "Kind of Blue",
        cover = "https://test.com/img.jpg",
        releaseDate = "1959-01-01T00:00:00.000Z",
        description = "Modal jazz masterpiece",
        genre = "Classical",
        recordLabel = "Sony Music"
    )

    private fun viewModelWith(
        repoBlock: AlbumRepository.() -> Unit = {}
    ): CreateAlbumViewModel {
        val repo = mockk<AlbumRepository>(relaxed = true).apply(repoBlock)
        return CreateAlbumViewModel(repo)
    }

    private fun setScreen(
        viewModel: CreateAlbumViewModel,
        onSuccess: () -> Unit = {},
        onDiscard: () -> Unit = {}
    ) {
        composeTestRule.setContent {
            VinilosTheme {
                CreateAlbumScreen(
                    viewModel = viewModel,
                    onSuccess = onSuccess,
                    onDiscard = onDiscard
                )
            }
        }
    }

    // ─── T1: La pantalla renderiza los elementos principales ─────────────────
    // Usa assertExists() porque el formulario hace scroll y algunos nodos
    // pueden estar fuera de la ventana visible.

    @Test
    fun createAlbumScreen_muestraElementosPrincipales() {
        setScreen(viewModelWith())

        composeTestRule.onNodeWithTag("create_album_screen").assertIsDisplayed()
        composeTestRule.onNodeWithText("Crear Álbum").assertIsDisplayed()
        composeTestRule.onNodeWithText("CATALOGING SYSTEM").assertIsDisplayed()
        composeTestRule.onNodeWithTag("input_name").assertExists()
        composeTestRule.onNodeWithTag("input_release_date").assertExists()
        composeTestRule.onNodeWithTag("dropdown_genre").assertExists()
        composeTestRule.onNodeWithTag("dropdown_record_label").assertExists()
        composeTestRule.onNodeWithTag("input_description").assertExists()
        composeTestRule.onNodeWithTag("btn_submit_album").assertExists()
        composeTestRule.onNodeWithTag("btn_discard_album").assertExists()
    }

    // ─── T2: Submit vacío muestra errores de validación ──────────────────────

    @Test
    fun createAlbumScreen_muestraErroresDeValidacion_conFormularioVacio() {
        setScreen(viewModelWith())

        composeTestRule.onNodeWithTag("btn_submit_album").performScrollTo().performClick()

        composeTestRule.waitUntil(timeoutMillis = 3_000) {
            composeTestRule
                .onAllNodes(hasTestTag("input_name"))
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithText("El título es obligatorio").assertExists()
        composeTestRule.onNodeWithText("El año es obligatorio").assertExists()
        composeTestRule.onNodeWithText("Selecciona un género").assertExists()
        composeTestRule.onNodeWithText("Selecciona un sello discográfico").assertExists()
        composeTestRule.onNodeWithText("La descripción es obligatoria").assertExists()
    }

    // ─── T3: Formulario completo y submit exitoso invoca onSuccess ────────────

    @Test
    fun createAlbumScreen_submitExitoso_invocaOnSuccess() {
        var successInvoked = false
        val vm = viewModelWith {
            coEvery { createAlbum(any()) } returns Result.success(createdAlbum)
        }
        setScreen(vm, onSuccess = { successInvoked = true })

        composeTestRule.onNodeWithTag("input_name").performScrollTo().performTextInput("Kind of Blue")
        composeTestRule.onNodeWithTag("input_release_date").performScrollTo().performTextInput("1959")
        composeTestRule.onNodeWithTag("input_description").performScrollTo().performTextInput("Modal jazz masterpiece")

        composeTestRule.onNodeWithTag("dropdown_genre").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("option_Classical").performClick()

        composeTestRule.onNodeWithTag("dropdown_record_label").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("option_Sony Music").performClick()

        composeTestRule.onNodeWithTag("btn_submit_album").performScrollTo().performClick()

        composeTestRule.waitUntil(timeoutMillis = 5_000) { successInvoked }

        assertTrue(successInvoked)
    }

    // ─── T4: Botón descartar invoca onDiscard ─────────────────────────────────

    @Test
    fun createAlbumScreen_botonDescartar_invocaOnDiscard() {
        var discardInvoked = false
        setScreen(viewModelWith(), onDiscard = { discardInvoked = true })

        composeTestRule.onNodeWithTag("btn_discard_album").performScrollTo().performClick()

        assertTrue(discardInvoked)
    }

    // ─── T5: Error del repositorio muestra mensaje de error ───────────────────

    @Test
    fun createAlbumScreen_muestraMensajeDeError_cuandoRepositorioFalla() {
        val vm = viewModelWith {
            coEvery { createAlbum(any()) } returns Result.failure(
                java.io.IOException("sin red")
            )
        }
        setScreen(vm)

        composeTestRule.onNodeWithTag("input_name").performScrollTo().performTextInput("Kind of Blue")
        composeTestRule.onNodeWithTag("input_release_date").performScrollTo().performTextInput("1959")
        composeTestRule.onNodeWithTag("input_description").performScrollTo().performTextInput("Modal jazz masterpiece")

        composeTestRule.onNodeWithTag("dropdown_genre").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("option_Classical").performClick()

        composeTestRule.onNodeWithTag("dropdown_record_label").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("option_Sony Music").performClick()

        composeTestRule.onNodeWithTag("btn_submit_album").performScrollTo().performClick()

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule
                .onAllNodes(hasTestTag("create_album_error"))
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithTag("create_album_error").assertExists()
    }

    // ─── T6: Botón deshabilitado durante carga ────────────────────────────────
    // El clock se pausa DESPUÉS del click para capturar el estado Loading
    // antes de que la coroutine complete.

    @Test
    fun createAlbumScreen_botonDeshabilitado_duranteCarga() {
        val vm = viewModelWith {
            coEvery { createAlbum(any()) } coAnswers {
                delay(10_000)
                Result.success(createdAlbum)
            }
        }
        setScreen(vm)

        composeTestRule.onNodeWithTag("input_name").performScrollTo().performTextInput("Kind of Blue")
        composeTestRule.onNodeWithTag("input_release_date").performScrollTo().performTextInput("1959")
        composeTestRule.onNodeWithTag("input_description").performScrollTo().performTextInput("Modal jazz masterpiece")

        composeTestRule.onNodeWithTag("dropdown_genre").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("option_Classical").performClick()

        composeTestRule.onNodeWithTag("dropdown_record_label").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("option_Sony Music").performClick()

        // Click y luego pausar el clock para quedarnos en estado Loading
        composeTestRule.onNodeWithTag("btn_submit_album").performScrollTo().performClick()
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.mainClock.advanceTimeBy(100)

        composeTestRule.onNodeWithTag("btn_submit_album").assertIsNotEnabled()
    }
}
