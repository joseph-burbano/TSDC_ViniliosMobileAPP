package com.uniandes.vinilos.ui.prizes

import com.uniandes.vinilos.model.PerformerPrize
import com.uniandes.vinilos.model.Prize
import com.uniandes.vinilos.repository.PrizeRepository
import com.uniandes.vinilos.testing.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class PrizeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val samplePrizes = listOf(
        Prize(1, "Grammy", "Music award", "NARAS"),
        Prize(2, "Latin Grammy", "Latin music award", "LARAS")
    )

    @Test
    fun `init carga la lista de premios`() = runTest {
        val repo = mockk<PrizeRepository>(relaxed = true)
        coEvery { repo.getPrizes() } returns samplePrizes

        val vm = PrizeViewModel(repo)
        advanceUntilIdle()

        assertEquals(2, vm.prizes.value.size)
        assertFalse(vm.isLoading.value)
        assertNull(vm.error.value)
        coVerify(exactly = 1) { repo.getPrizes() }
    }

    @Test
    fun `IOException expone mensaje sin conexion`() = runTest {
        val repo = mockk<PrizeRepository>(relaxed = true)
        coEvery { repo.getPrizes() } throws IOException("offline")

        val vm = PrizeViewModel(repo)
        advanceUntilIdle()

        assertEquals(
            "Sin conexión. Revisa tu red e inténtalo de nuevo.",
            vm.error.value
        )
    }

    @Test
    fun `HttpException expone el codigo del servidor`() = runTest {
        val httpError = HttpException(
            Response.error<Any>(500, "".toResponseBody("application/json".toMediaTypeOrNull()))
        )
        val repo = mockk<PrizeRepository>(relaxed = true)
        coEvery { repo.getPrizes() } throws httpError

        val vm = PrizeViewModel(repo)
        advanceUntilIdle()

        assertEquals(
            "El servidor respondió con un error (500).",
            vm.error.value
        )
    }

    @Test
    fun `submitAssociation con fecha vacia produce error sin llamar al repo`() = runTest {
        val repo = mockk<PrizeRepository>(relaxed = true)
        coEvery { repo.getPrizes() } returns samplePrizes
        val vm = PrizeViewModel(repo)
        advanceUntilIdle()

        vm.submitAssociation(
            performerId = 1,
            isMusician = true,
            premiationDate = "",
            selectedPrizeId = 1,
            newPrizeName = null,
            newPrizeDescription = null,
            newPrizeOrganization = null
        )
        advanceUntilIdle()

        assertEquals("La fecha de premiación es obligatoria.", vm.error.value)
        coVerify(exactly = 0) { repo.associatePrizeToPerformer(any(), any(), any(), any()) }
    }

    @Test
    fun `submitAssociation sin seleccion ni nuevo premio produce error`() = runTest {
        val repo = mockk<PrizeRepository>(relaxed = true)
        coEvery { repo.getPrizes() } returns samplePrizes
        val vm = PrizeViewModel(repo)
        advanceUntilIdle()

        vm.submitAssociation(
            performerId = 1,
            isMusician = true,
            premiationDate = "2024-01-01",
            selectedPrizeId = null,
            newPrizeName = "",
            newPrizeDescription = "",
            newPrizeOrganization = ""
        )
        advanceUntilIdle()

        assertNotNull(vm.error.value)
        coVerify(exactly = 0) { repo.associatePrizeToPerformer(any(), any(), any(), any()) }
    }

    @Test
    fun `submitAssociation con premio existente asocia y expone success`() = runTest {
        val repo = mockk<PrizeRepository>(relaxed = true)
        coEvery { repo.getPrizes() } returns samplePrizes
        coEvery {
            repo.associatePrizeToPerformer(prizeId = 1, performerId = 99, isMusician = true, premiationDate = "2024-05-01")
        } returns PerformerPrize(id = 7, premiationDate = "2024-05-01")

        val vm = PrizeViewModel(repo)
        advanceUntilIdle()

        vm.submitAssociation(
            performerId = 99,
            isMusician = true,
            premiationDate = "2024-05-01",
            selectedPrizeId = 1,
            newPrizeName = null,
            newPrizeDescription = null,
            newPrizeOrganization = null
        )
        advanceUntilIdle()

        assertNotNull(vm.associationSuccess.value)
        assertEquals(7, vm.associationSuccess.value!!.id)
        coVerify(exactly = 0) { repo.createPrize(any(), any(), any()) }
    }

    @Test
    fun `submitAssociation crea un premio nuevo antes de asociar`() = runTest {
        val repo = mockk<PrizeRepository>(relaxed = true)
        val createdPrize = Prize(99, "Mercury Prize", "UK", "BPI")
        coEvery { repo.getPrizes() } returns samplePrizes
        coEvery { repo.createPrize("Mercury Prize", "UK album of the year", "BPI") } returns createdPrize
        coEvery {
            repo.associatePrizeToPerformer(
                prizeId = 99,
                performerId = 5,
                isMusician = false,
                premiationDate = "2024-09-10"
            )
        } returns PerformerPrize(id = 42, premiationDate = "2024-09-10")

        val vm = PrizeViewModel(repo)
        advanceUntilIdle()

        vm.submitAssociation(
            performerId = 5,
            isMusician = false,
            premiationDate = "2024-09-10",
            selectedPrizeId = null,
            newPrizeName = "Mercury Prize",
            newPrizeDescription = "UK album of the year",
            newPrizeOrganization = "BPI"
        )
        advanceUntilIdle()

        // El premio nuevo aparece en la lista visible para que la UI no parpadee
        assertEquals(3, vm.prizes.value.size)
        assertEquals(99, vm.prizes.value.last().id)
        assertNotNull(vm.associationSuccess.value)
        coVerify(exactly = 1) { repo.createPrize("Mercury Prize", "UK album of the year", "BPI") }
        coVerify(exactly = 1) {
            repo.associatePrizeToPerformer(99, 5, false, "2024-09-10")
        }
    }

    @Test
    fun `consumeAssociationSuccess limpia el flow de success`() = runTest {
        val repo = mockk<PrizeRepository>(relaxed = true)
        coEvery { repo.getPrizes() } returns samplePrizes
        coEvery {
            repo.associatePrizeToPerformer(any(), any(), any(), any())
        } returns PerformerPrize(id = 1, premiationDate = "2024-01-01")

        val vm = PrizeViewModel(repo)
        advanceUntilIdle()

        vm.submitAssociation(
            performerId = 1, isMusician = true, premiationDate = "2024-01-01",
            selectedPrizeId = 1, newPrizeName = null,
            newPrizeDescription = null, newPrizeOrganization = null
        )
        advanceUntilIdle()
        assertNotNull(vm.associationSuccess.value)

        vm.consumeAssociationSuccess()
        assertNull(vm.associationSuccess.value)
    }

    @Test
    fun `clearError limpia el mensaje de error`() = runTest {
        val repo = mockk<PrizeRepository>(relaxed = true)
        coEvery { repo.getPrizes() } throws IOException("offline")

        val vm = PrizeViewModel(repo)
        advanceUntilIdle()
        assertNotNull(vm.error.value)

        vm.clearError()
        assertNull(vm.error.value)
    }
}
