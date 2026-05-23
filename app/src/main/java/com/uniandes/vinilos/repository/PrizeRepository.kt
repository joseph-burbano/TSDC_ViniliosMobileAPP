package com.uniandes.vinilos.repository

import com.uniandes.vinilos.database.dao.PrizeDao
import com.uniandes.vinilos.database.toEntity
import com.uniandes.vinilos.database.toPrize
import com.uniandes.vinilos.model.PerformerPrize
import com.uniandes.vinilos.model.Prize
import com.uniandes.vinilos.network.NetworkServiceAdapter
import com.uniandes.vinilos.network.PerformerPrizeBody
import com.uniandes.vinilos.network.PrizeCreateBody
import com.uniandes.vinilos.network.VinilosApi

class PrizeRepository(
    private val dao: PrizeDao,
    private val api: VinilosApi = NetworkServiceAdapter.api
) {
    suspend fun getPrizes(): List<Prize> {
        val cached = dao.getAll()
        if (cached.isNotEmpty()) {
            return cached.map { it.toPrize() }
        }
        val remote = api.getPrizes()
        dao.insertAll(remote.map { it.toEntity() })
        return remote
    }

    suspend fun refreshPrizes(): List<Prize> {
        dao.deleteAll()
        val remote = api.getPrizes()
        dao.insertAll(remote.map { it.toEntity() })
        return remote
    }

    suspend fun createPrize(name: String, description: String, organization: String): Prize {
        val created = api.createPrize(
            PrizeCreateBody(name = name, description = description, organization = organization)
        )
        dao.insert(created.toEntity())
        return created
    }

    /**
     * Routes the association call to the right endpoint depending on whether the
     * performer is a musician (`birthDate != null`) or a band (only `creationDate`).
     * This mirrors the heuristic the rest of the app already uses to label artists.
     */
    suspend fun associatePrizeToPerformer(
        prizeId: Int,
        performerId: Int,
        isMusician: Boolean,
        premiationDate: String
    ): PerformerPrize {
        val body = PerformerPrizeBody(premiationDate = premiationDate)
        return if (isMusician) {
            api.associatePrizeToMusician(prizeId, performerId, body)
        } else {
            api.associatePrizeToBand(prizeId, performerId, body)
        }
    }
}
