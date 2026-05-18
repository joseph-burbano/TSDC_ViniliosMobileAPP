package com.uniandes.vinilos.ui.albums

import com.uniandes.vinilos.model.CreateTrackRequest
import com.uniandes.vinilos.model.Track
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
class CreateTrackViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val albumId = 7
    private val createdTrack = Track(
        id = 101,
        name = "So What",
        duration = "9:22"
    )

    private fun viewModel(repoBlock: AlbumRepository.() -> Unit = {}): CreateTrackViewModel {
        val repo = mockk<AlbumRepository>(relaxed = true).apply(repoBlock)
        return CreateTrackViewModel(repo)
    }

    // ─── Estado inicial ───────────────────────────────────────────────────────

    @Test
    fun `estado inicial es Idle`() = runTest {
        val vm = viewModel()
        assertTrue(vm.uiState.value is CreateTrackUiState.Idle)
        assertEquals("", vm.name.value)
        assertEquals("", vm.duration.value)
        assertNull(vm.nameError.value)
        assertNull(vm.durationError.value)
    }

    // ─── Validaciones ─────────────────────────────────────────────────────────

    @Test
    fun `submitTrack con formulario vacio no llama al repositorio`() = runTest {
        val repo = mockk<AlbumRepository>(relaxed = true)
        val vm = CreateTrackViewModel(repo)

        vm.submitTrack(albumId)
        advanceUntilIdle()

        coVerify(exactly = 0) { repo.addTrackToAlbum(any(), any()) }
        assertTrue(vm.uiState.value is CreateTrackUiState.Idle)
    }

    @Test
    fun `submitTrack con nombre vacio muestra error de nombre`() = runTest {
        val vm = viewModel()
        vm.duration.value = "3:45"

        vm.submitTrack(albumId)
        advanceUntilIdle()

        assertEquals("El nombre de la canción es obligatorio", vm.nameError.value)
        assertTrue(vm.uiState.value is CreateTrackUiState.Idle)
    }

    @Test
    fun `submitTrack con nombre demasiado largo muestra error`() = runTest {
        val vm = viewModel()
        vm.name.value = "x".repeat(81)
        vm.duration.value = "3:45"

        vm.submitTrack(albumId)
        advanceUntilIdle()

        assertEquals("El nombre no puede superar 80 caracteres", vm.nameError.value)
    }

    @Test
    fun `submitTrack con duracion vacia muestra error de duracion`() = runTest {
        val vm = viewModel()
        vm.name.value = "So What"

        vm.submitTrack(albumId)
        advanceUntilIdle()

        assertEquals("La duración es obligatoria", vm.durationError.value)
    }

    @Test
    fun `submitTrack con formato de duracion invalido muestra error`() = runTest {
        val vm = viewModel()
        vm.name.value = "So What"
        vm.duration.value = "9 minutos"

        vm.submitTrack(albumId)
        advanceUntilIdle()

        assertEquals("Formato esperado mm:ss (ej. 3:45)", vm.durationError.value)
    }

    @Test
    fun `submitTrack con segundos mayores a 59 muestra error de formato`() = runTest {
        val vm = viewModel()
        vm.name.value = "So What"
        vm.duration.value = "3:75"

        vm.submitTrack(albumId)
        advanceUntilIdle()

        assertEquals("Formato esperado mm:ss (ej. 3:45)", vm.durationError.value)
    }

    @Test
    fun `submitTrack acepta duracion en formato m_ss`() = runTest {
        val vm = viewModel {
            coEvery { addTrackToAlbum(albumId, any()) } returns Result.success(createdTrack)
        }
        vm.name.value = "Cancion corta"
        vm.duration.value = "0:30"

        vm.submitTrack(albumId)
        advanceUntilIdle()

        assertNull(vm.durationError.value)
        assertTrue(vm.uiState.value is CreateTrackUiState.Success)
    }

    @Test
    fun `submitTrack acepta duracion en formato mm_ss largo`() = runTest {
        val vm = viewModel {
            coEvery { addTrackToAlbum(albumId, any()) } returns Result.success(createdTrack)
        }
        vm.name.value = "Cancion larga"
        vm.duration.value = "12:08"

        vm.submitTrack(albumId)
        advanceUntilIdle()

        assertNull(vm.durationError.value)
        assertTrue(vm.uiState.value is CreateTrackUiState.Success)
    }

    // ─── Flujo exitoso ────────────────────────────────────────────────────────

    @Test
    fun `submitTrack con datos validos emite Success con el track creado`() = runTest {
        val vm = viewModel {
            coEvery { addTrackToAlbum(albumId, any()) } returns Result.success(createdTrack)
        }
        vm.name.value = "So What"
        vm.duration.value = "9:22"

        vm.submitTrack(albumId)
        advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state is CreateTrackUiState.Success)
        assertEquals(101, (state as CreateTrackUiState.Success).track.id)
        assertEquals("So What", state.track.name)
    }

    @Test
    fun `submitTrack envia el albumId correcto al repositorio`() = runTest {
        var capturedAlbumId: Int? = null
        var capturedRequest: CreateTrackRequest? = null
        val repo = mockk<AlbumRepository>(relaxed = true)
        coEvery { repo.addTrackToAlbum(any(), any()) } coAnswers {
            capturedAlbumId = firstArg()
            capturedRequest = secondArg()
            Result.success(createdTrack)
        }
        val vm = CreateTrackViewModel(repo)
        vm.name.value = "  So What  " // espacios al margen
        vm.duration.value = " 9:22 "

        vm.submitTrack(albumId)
        advanceUntilIdle()

        assertEquals(albumId, capturedAlbumId)
        // Verifica que el ViewModel hace trim antes de enviar
        assertEquals("So What", capturedRequest?.name)
        assertEquals("9:22", capturedRequest?.duration)
    }

    // ─── Flujo de error ───────────────────────────────────────────────────────

    @Test
    fun `submitTrack emite Error cuando el repositorio falla`() = runTest {
        val vm = viewModel {
            coEvery { addTrackToAlbum(albumId, any()) } returns Result.failure(IOException("sin red"))
        }
        vm.name.value = "So What"
        vm.duration.value = "9:22"

        vm.submitTrack(albumId)
        advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state is CreateTrackUiState.Error)
        assertEquals("sin red", (state as CreateTrackUiState.Error).message)
    }

    @Test
    fun `submitTrack emite Error con mensaje generico si la excepcion no tiene mensaje`() = runTest {
        val vm = viewModel {
            coEvery { addTrackToAlbum(albumId, any()) } returns Result.failure(RuntimeException())
        }
        vm.name.value = "So What"
        vm.duration.value = "9:22"

        vm.submitTrack(albumId)
        advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state is CreateTrackUiState.Error)
        assertEquals("Error al asociar la canción", (state as CreateTrackUiState.Error).message)
    }

    // ─── resetState ───────────────────────────────────────────────────────────

    @Test
    fun `resetState vuelve a Idle y limpia campos y errores`() = runTest {
        val vm = viewModel {
            coEvery { addTrackToAlbum(albumId, any()) } returns Result.success(createdTrack)
        }
        vm.name.value = "So What"
        vm.duration.value = "9:22"

        vm.submitTrack(albumId)
        advanceUntilIdle()

        vm.resetState()

        assertTrue(vm.uiState.value is CreateTrackUiState.Idle)
        assertEquals("", vm.name.value)
        assertEquals("", vm.duration.value)
        assertNull(vm.nameError.value)
        assertNull(vm.durationError.value)
    }
}
