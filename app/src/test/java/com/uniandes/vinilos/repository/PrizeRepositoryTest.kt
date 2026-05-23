package com.uniandes.vinilos.repository

import com.uniandes.vinilos.database.dao.PrizeDao
import com.uniandes.vinilos.database.entities.PrizeEntity
import com.uniandes.vinilos.model.PerformerPrize
import com.uniandes.vinilos.model.Prize
import com.uniandes.vinilos.network.PerformerPrizeBody
import com.uniandes.vinilos.network.PrizeCreateBody
import com.uniandes.vinilos.network.VinilosApi
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class PrizeRepositoryTest {

    private lateinit var dao: PrizeDao
    private lateinit var api: VinilosApi
    private lateinit var repository: PrizeRepository

    private val cachedEntities = listOf(
        PrizeEntity(1, "Grammy", "Music recognition", "NARAS"),
        PrizeEntity(2, "Latin Grammy", "Latin music recognition", "LARAS")
    )

    private val remotePrizes = listOf(
        Prize(10, "Polar Music Prize", "International prize", "Royal Swedish Academy")
    )

    @Before
    fun setup() {
        dao = mockk(relaxed = true)
        api = mockk(relaxed = true)
        repository = PrizeRepository(dao, api)
    }

    @Test
    fun `getPrizes returns cached data when dao is not empty`() = runTest {
        coEvery { dao.getAll() } returns cachedEntities

        val result = repository.getPrizes()

        assertEquals(2, result.size)
        assertEquals("Grammy", result[0].name)
        coVerify(exactly = 0) { api.getPrizes() }
    }

    @Test
    fun `getPrizes hits the api and caches when dao is empty`() = runTest {
        coEvery { dao.getAll() } returns emptyList()
        coEvery { api.getPrizes() } returns remotePrizes

        val result = repository.getPrizes()

        assertEquals(1, result.size)
        assertEquals("Polar Music Prize", result[0].name)
        coVerify(exactly = 1) { api.getPrizes() }
        coVerify(exactly = 1) { dao.insertAll(any()) }
    }

    @Test
    fun `refreshPrizes deletes cache and refetches`() = runTest {
        coEvery { api.getPrizes() } returns remotePrizes

        repository.refreshPrizes()

        coVerify(exactly = 1) { dao.deleteAll() }
        coVerify(exactly = 1) { api.getPrizes() }
        coVerify(exactly = 1) { dao.insertAll(any()) }
    }

    @Test
    fun `createPrize posts to api and caches the created prize`() = runTest {
        val created = Prize(50, "Mercury Prize", "UK prize", "BPI")
        coEvery { api.createPrize(any()) } returns created

        val result = repository.createPrize(
            name = "Mercury Prize",
            description = "UK prize",
            organization = "BPI"
        )

        assertEquals(50, result.id)
        coVerify(exactly = 1) {
            api.createPrize(PrizeCreateBody("Mercury Prize", "UK prize", "BPI"))
        }
        coVerify(exactly = 1) { dao.insert(any()) }
    }

    @Test
    fun `associatePrizeToPerformer routes musicians to the musicians endpoint`() = runTest {
        val response = PerformerPrize(1, "2024-01-15")
        coEvery { api.associatePrizeToMusician(any(), any(), any()) } returns response

        repository.associatePrizeToPerformer(
            prizeId = 5,
            performerId = 99,
            isMusician = true,
            premiationDate = "2024-01-15"
        )

        coVerify(exactly = 1) {
            api.associatePrizeToMusician(5, 99, PerformerPrizeBody("2024-01-15"))
        }
        coVerify(exactly = 0) { api.associatePrizeToBand(any(), any(), any()) }
    }

    @Test
    fun `associatePrizeToPerformer routes bands to the bands endpoint`() = runTest {
        val response = PerformerPrize(2, "1999-12-31")
        coEvery { api.associatePrizeToBand(any(), any(), any()) } returns response

        repository.associatePrizeToPerformer(
            prizeId = 7,
            performerId = 42,
            isMusician = false,
            premiationDate = "1999-12-31"
        )

        coVerify(exactly = 1) {
            api.associatePrizeToBand(7, 42, PerformerPrizeBody("1999-12-31"))
        }
        coVerify(exactly = 0) { api.associatePrizeToMusician(any(), any(), any()) }
    }
}
