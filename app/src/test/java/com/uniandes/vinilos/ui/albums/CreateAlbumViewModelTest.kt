package com.uniandes.vinilos.ui.albums

import com.uniandes.vinilos.model.Album
import com.uniandes.vinilos.model.CreateAlbumRequest
import com.uniandes.vinilos.repository.AlbumRepository
import com.uniandes.vinilos.testing.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class CreateAlbumViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val createdAlbum = Album(
        id = 99,
        name = "Kind of Blue",
        cover = "https://test.com/img.jpg",
        releaseDate = "1959-01-01T00:00:00.000Z",
        description = "Modal jazz masterpiece",
        genre = "Classical",
        recordLabel = "Sony Music"
    )

    private fun viewModel(repoBlock: AlbumRepository.() -> Unit = {}): CreateAlbumViewModel {
        val repo = mockk<AlbumRepository>(relaxed = true).apply(repoBlock)
        return CreateAlbumViewModel(repo)
    }

    // ─── Estado inicial ───────────────────────────────────────────────────────

    @Test
    fun `estado inicial es Idle`() = runTest {
        val vm = viewModel()
        assertTrue(vm.uiState.value is CreateAlbumUiState.Idle)
    }

    @Test
    fun `campos iniciales estan vacios`() = runTest {
        val vm = viewModel()
        assertEquals("", vm.name.value)
        assertEquals("", vm.cover.value)
        assertEquals("", vm.releaseDate.value)
        assertEquals("", vm.description.value)
        assertEquals("", vm.genre.value)
        assertEquals("", vm.recordLabel.value)
    }

    // ─── Validaciones ─────────────────────────────────────────────────────────

    @Test
    fun `submitAlbum con formulario vacio no llama al repositorio`() = runTest {
        val repo = mockk<AlbumRepository>(relaxed = true)
        val vm = CreateAlbumViewModel(repo)

        vm.submitAlbum()
        advanceUntilIdle()

        coVerify(exactly = 0) { repo.createAlbum(any()) }
        assertTrue(vm.uiState.value is CreateAlbumUiState.Idle)
    }

    @Test
    fun `submitAlbum con nombre vacio muestra error de nombre`() = runTest {
        val vm = viewModel()
        vm.genre.value = "Rock"
        vm.recordLabel.value = "Sony Music"
        vm.releaseDate.value = "1969"
        vm.description.value = "Descripcion"

        vm.submitAlbum()
        advanceUntilIdle()

        assertEquals("El título es obligatorio", vm.nameError.value)
    }

    @Test
    fun `submitAlbum con año no numerico muestra error`() = runTest {
        val vm = viewModel()
        vm.name.value = "Abbey Road"
        vm.releaseDate.value = "abc"
        vm.genre.value = "Rock"
        vm.recordLabel.value = "Sony Music"
        vm.description.value = "Descripcion"

        vm.submitAlbum()
        advanceUntilIdle()

        assertEquals("Ingresa un año válido", vm.releaseDateError.value)
    }

    @Test
    fun `submitAlbum con año fuera de rango muestra error`() = runTest {
        val vm = viewModel()
        vm.name.value = "Abbey Road"
        vm.releaseDate.value = "1800"
        vm.genre.value = "Rock"
        vm.recordLabel.value = "Sony Music"
        vm.description.value = "Descripcion"

        vm.submitAlbum()
        advanceUntilIdle()

        assertEquals("El año debe estar entre 1900 y 2025", vm.releaseDateError.value)
    }

    @Test
    fun `submitAlbum sin genero muestra error de genero`() = runTest {
        val vm = viewModel()
        vm.name.value = "Abbey Road"
        vm.releaseDate.value = "1969"
        vm.recordLabel.value = "Sony Music"
        vm.description.value = "Descripcion"

        vm.submitAlbum()
        advanceUntilIdle()

        assertEquals("Selecciona un género", vm.genreError.value)
    }

    @Test
    fun `submitAlbum sin sello muestra error de sello`() = runTest {
        val vm = viewModel()
        vm.name.value = "Abbey Road"
        vm.releaseDate.value = "1969"
        vm.genre.value = "Rock"
        vm.description.value = "Descripcion"

        vm.submitAlbum()
        advanceUntilIdle()

        assertEquals("Selecciona un sello discográfico", vm.recordLabelError.value)
    }

    @Test
    fun `submitAlbum sin descripcion muestra error de descripcion`() = runTest {
        val vm = viewModel()
        vm.name.value = "Abbey Road"
        vm.releaseDate.value = "1969"
        vm.genre.value = "Rock"
        vm.recordLabel.value = "Sony Music"

        vm.submitAlbum()
        advanceUntilIdle()

        assertEquals("La descripción es obligatoria", vm.descriptionError.value)
    }

    // ─── Flujo exitoso ────────────────────────────────────────────────────────

    @Test
    fun `submitAlbum con datos validos emite Success`() = runTest {
        val vm = viewModel {
            coEvery { createAlbum(any()) } returns Result.success(createdAlbum)
        }
        vm.name.value = "Kind of Blue"
        vm.releaseDate.value = "1959"
        vm.genre.value = "Classical"
        vm.recordLabel.value = "Sony Music"
        vm.description.value = "Modal jazz masterpiece"

        vm.submitAlbum()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state is CreateAlbumUiState.Success)
        assertEquals(99, (state as CreateAlbumUiState.Success).album.id)
    }

    @Test
    fun `submitAlbum construye fecha ISO correctamente desde el año`() = runTest {
        var capturedRequest: CreateAlbumRequest? = null
        val repo = mockk<AlbumRepository>(relaxed = true)
        coEvery { repo.createAlbum(any()) } coAnswers {
            capturedRequest = firstArg()
            Result.success(createdAlbum)
        }
        val vm = CreateAlbumViewModel(repo)
        vm.name.value = "Kind of Blue"
        vm.releaseDate.value = "1959"
        vm.genre.value = "Classical"
        vm.recordLabel.value = "Sony Music"
        vm.description.value = "Modal jazz masterpiece"

        vm.submitAlbum()
        advanceUntilIdle()

        assertEquals("1959-01-01T00:00:00.000Z", capturedRequest?.releaseDate)
    }

    @Test
    fun `submitAlbum sin cover usa placeholder`() = runTest {
        var capturedRequest: CreateAlbumRequest? = null
        val repo = mockk<AlbumRepository>(relaxed = true)
        coEvery { repo.createAlbum(any()) } coAnswers {
            capturedRequest = firstArg()
            Result.success(createdAlbum)
        }
        val vm = CreateAlbumViewModel(repo)
        vm.name.value = "Kind of Blue"
        vm.releaseDate.value = "1959"
        vm.genre.value = "Classical"
        vm.recordLabel.value = "Sony Music"
        vm.description.value = "Modal jazz masterpiece"
        // cover vacío a propósito

        vm.submitAlbum()
        advanceUntilIdle()

        assertTrue(capturedRequest?.cover?.isNotBlank() == true)
    }

    // ─── Flujo de error ───────────────────────────────────────────────────────

    @Test
    fun `submitAlbum emite Error cuando el repositorio falla`() = runTest {
        val vm = viewModel {
            coEvery { createAlbum(any()) } returns Result.failure(IOException("sin red"))
        }
        vm.name.value = "Kind of Blue"
        vm.releaseDate.value = "1959"
        vm.genre.value = "Classical"
        vm.recordLabel.value = "Sony Music"
        vm.description.value = "Modal jazz masterpiece"

        vm.submitAlbum()
        advanceUntilIdle()

        assertTrue(vm.uiState.value is CreateAlbumUiState.Error)
    }

    // ─── resetState ───────────────────────────────────────────────────────────

    @Test
    fun `resetState vuelve el estado a Idle`() = runTest {
        val vm = viewModel {
            coEvery { createAlbum(any()) } returns Result.success(createdAlbum)
        }
        vm.name.value = "Kind of Blue"
        vm.releaseDate.value = "1959"
        vm.genre.value = "Classical"
        vm.recordLabel.value = "Sony Music"
        vm.description.value = "Modal jazz masterpiece"

        vm.submitAlbum()
        advanceUntilIdle()

        vm.resetState()
        assertTrue(vm.uiState.value is CreateAlbumUiState.Idle)
    }
}
