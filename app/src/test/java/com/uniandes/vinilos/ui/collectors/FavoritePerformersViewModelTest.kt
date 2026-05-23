package com.uniandes.vinilos.ui.collectors

import com.uniandes.vinilos.model.Performer
import com.uniandes.vinilos.repository.ArtistRepository
import com.uniandes.vinilos.repository.CollectorRepository
import com.uniandes.vinilos.testing.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class FavoritePerformersViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val musician = Performer(
        id = 1, name = "Miles Davis", image = "",
        description = "Jazz trumpeter", birthDate = "1926-05-26", type = "musician"
    )
    private val band = Performer(
        id = 2, name = "The Beatles", image = "",
        description = "Rock band", creationDate = "1960-01-01", type = "band"
    )

    private fun buildVm(
        collectorRepo: CollectorRepository = mockk(relaxed = true),
        artistRepo: ArtistRepository = mockk(relaxed = true)
    ) = FavoritePerformersViewModel(collectorRepo, artistRepo)

    // ── loadData ──────────────────────────────────────────────────────────────

    @Test
    fun `loadData establece favoriteIds a partir de initialFavorites`() = runTest {
        val artistRepo = mockk<ArtistRepository>(relaxed = true)
        coEvery { artistRepo.getPerformers() } returns listOf(musician, band)
        val vm = buildVm(artistRepo = artistRepo)

        vm.loadData(1, listOf(musician))
        advanceUntilIdle()

        assertTrue(vm.favoriteIds.value.contains(musician.id))
        assertFalse(vm.favoriteIds.value.contains(band.id))
    }

    @Test
    fun `loadData carga todos los artistas disponibles`() = runTest {
        val artistRepo = mockk<ArtistRepository>(relaxed = true)
        coEvery { artistRepo.getPerformers() } returns listOf(musician, band)
        val vm = buildVm(artistRepo = artistRepo)

        vm.loadData(1, emptyList())
        advanceUntilIdle()

        assertEquals(2, vm.allPerformers.value.size)
        assertFalse(vm.isLoading.value)
        assertNull(vm.error.value)
    }

    @Test
    fun `loadData establece isLoading en false al terminar`() = runTest {
        val artistRepo = mockk<ArtistRepository>(relaxed = true)
        coEvery { artistRepo.getPerformers() } returns listOf(musician)
        val vm = buildVm(artistRepo = artistRepo)

        vm.loadData(1, emptyList())
        advanceUntilIdle()

        assertFalse(vm.isLoading.value)
    }

    @Test
    fun `loadData expone error de conexion cuando el repositorio falla con IOException`() = runTest {
        val artistRepo = mockk<ArtistRepository>(relaxed = true)
        coEvery { artistRepo.getPerformers() } throws IOException("sin red")
        val vm = buildVm(artistRepo = artistRepo)

        vm.loadData(1, emptyList())
        advanceUntilIdle()

        assertNotNull(vm.error.value)
        assertTrue(vm.error.value!!.contains("Sin conexión"))
        assertFalse(vm.isLoading.value)
    }

    @Test
    fun `loadData expone error HTTP cuando el repositorio falla con HttpException`() = runTest {
        val artistRepo = mockk<ArtistRepository>(relaxed = true)
        val httpError = HttpException(
            Response.error<Any>(500, "error".toResponseBody("text/plain".toMediaTypeOrNull()))
        )
        coEvery { artistRepo.getPerformers() } throws httpError
        val vm = buildVm(artistRepo = artistRepo)

        vm.loadData(1, emptyList())
        advanceUntilIdle()

        assertNotNull(vm.error.value)
        assertTrue(vm.error.value!!.contains("500"))
    }

    // ── toggleFavorite – agregar ───────────────────────────────────────────────

    @Test
    fun `toggleFavorite agrega artista a favoriteIds cuando no era favorito`() = runTest {
        val collectorRepo = mockk<CollectorRepository>(relaxed = true)
        coEvery { collectorRepo.addFavoritePerformer(1, musician) } returns musician
        val vm = buildVm(collectorRepo = collectorRepo)
        vm.loadData(1, emptyList())
        advanceUntilIdle()

        vm.toggleFavorite(1, musician)
        advanceUntilIdle()

        assertTrue(vm.favoriteIds.value.contains(musician.id))
        assertNull(vm.error.value)
    }

    @Test
    fun `toggleFavorite llama addFavoritePerformer cuando el artista no era favorito`() = runTest {
        val collectorRepo = mockk<CollectorRepository>(relaxed = true)
        coEvery { collectorRepo.addFavoritePerformer(1, musician) } returns musician
        val vm = buildVm(collectorRepo = collectorRepo)
        vm.loadData(1, emptyList())
        advanceUntilIdle()

        vm.toggleFavorite(1, musician)
        advanceUntilIdle()

        coVerify(exactly = 1) { collectorRepo.addFavoritePerformer(1, musician) }
    }

    // ── toggleFavorite – eliminar ──────────────────────────────────────────────

    @Test
    fun `toggleFavorite elimina artista de favoriteIds cuando ya era favorito`() = runTest {
        val collectorRepo = mockk<CollectorRepository>(relaxed = true)
        val vm = buildVm(collectorRepo = collectorRepo)
        vm.loadData(1, listOf(musician))
        advanceUntilIdle()

        vm.toggleFavorite(1, musician)
        advanceUntilIdle()

        assertFalse(vm.favoriteIds.value.contains(musician.id))
    }

    @Test
    fun `toggleFavorite llama removeFavoritePerformer cuando el artista ya era favorito`() = runTest {
        val collectorRepo = mockk<CollectorRepository>(relaxed = true)
        val vm = buildVm(collectorRepo = collectorRepo)
        vm.loadData(1, listOf(musician))
        advanceUntilIdle()

        vm.toggleFavorite(1, musician)
        advanceUntilIdle()

        coVerify(exactly = 1) { collectorRepo.removeFavoritePerformer(1, musician) }
    }

    // ── toggleFavorite – revert en error ──────────────────────────────────────

    @Test
    fun `toggleFavorite revierte el estado optimista cuando addFavoritePerformer falla`() = runTest {
        val collectorRepo = mockk<CollectorRepository>(relaxed = true)
        coEvery { collectorRepo.addFavoritePerformer(any(), any()) } throws IOException("fallo de red")
        val vm = buildVm(collectorRepo = collectorRepo)
        vm.loadData(1, emptyList())
        advanceUntilIdle()

        vm.toggleFavorite(1, musician)
        advanceUntilIdle()

        assertFalse(vm.favoriteIds.value.contains(musician.id))
        assertNotNull(vm.error.value)
    }

    @Test
    fun `toggleFavorite revierte el estado optimista cuando removeFavoritePerformer falla`() = runTest {
        val collectorRepo = mockk<CollectorRepository>(relaxed = true)
        coEvery { collectorRepo.removeFavoritePerformer(any(), any()) } throws IOException("fallo de red")
        val vm = buildVm(collectorRepo = collectorRepo)
        vm.loadData(1, listOf(musician))
        advanceUntilIdle()

        vm.toggleFavorite(1, musician)
        advanceUntilIdle()

        assertTrue(vm.favoriteIds.value.contains(musician.id))
        assertNotNull(vm.error.value)
    }

    @Test
    fun `toggleFavorite establece isTogglingId a null al terminar`() = runTest {
        val collectorRepo = mockk<CollectorRepository>(relaxed = true)
        coEvery { collectorRepo.addFavoritePerformer(any(), any()) } returns musician
        val vm = buildVm(collectorRepo = collectorRepo)
        vm.loadData(1, emptyList())
        advanceUntilIdle()

        vm.toggleFavorite(1, musician)
        advanceUntilIdle()

        assertNull(vm.isTogglingId.value)
    }

    // ── clearError ────────────────────────────────────────────────────────────

    @Test
    fun `clearError limpia el estado de error`() = runTest {
        val artistRepo = mockk<ArtistRepository>(relaxed = true)
        coEvery { artistRepo.getPerformers() } throws IOException("fallo")
        val vm = buildVm(artistRepo = artistRepo)
        vm.loadData(1, emptyList())
        advanceUntilIdle()
        assertNotNull(vm.error.value)

        vm.clearError()

        assertNull(vm.error.value)
    }

    // ── estado inicial ────────────────────────────────────────────────────────

    @Test
    fun `estado inicial tiene listas vacias y sin error`() = runTest {
        val vm = buildVm()

        assertTrue(vm.allPerformers.value.isEmpty())
        assertTrue(vm.favoriteIds.value.isEmpty())
        assertFalse(vm.isLoading.value)
        assertNull(vm.isTogglingId.value)
        assertNull(vm.error.value)
    }
}
