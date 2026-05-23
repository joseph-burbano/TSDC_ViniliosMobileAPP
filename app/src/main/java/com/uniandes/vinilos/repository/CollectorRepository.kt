package com.uniandes.vinilos.repository

import com.uniandes.vinilos.database.dao.CollectorDao
import com.uniandes.vinilos.database.toCollector
import com.uniandes.vinilos.database.toEntity
import com.uniandes.vinilos.model.Collector
import com.uniandes.vinilos.model.Performer
import com.uniandes.vinilos.network.NetworkServiceAdapter
import com.uniandes.vinilos.network.VinilosApi

class CollectorRepository(
    private val dao: CollectorDao,
    private val api: VinilosApi = NetworkServiceAdapter.api
) {

    suspend fun getCollectors(): List<Collector> {
        val cached = dao.getAll()
        if (cached.isNotEmpty()) return cached.map { it.toCollector() }
        return fetchAndCache()
    }

    suspend fun refreshCollectors(): List<Collector> {
        // Preservar favoritos antes de borrar (el endpoint /collectors no los incluye)
        val preservedFavorites = dao.getAll().associateBy { it.id }
        dao.deleteAll()
        return fetchAndCache(preservedFavorites)
    }

    suspend fun getCollector(id: Int): Collector? {
        val cached = dao.findById(id)
        if (cached != null) return cached.toCollector()
        return try {
            val remote = api.getCollector(id)
            dao.insertAll(listOf(remote.toEntity()))
            remote
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Obtiene los artistas favoritos del coleccionista directamente desde el API,
     * sin pasar por la caché de Room (los favoritos se gestionan en la caché del
     * colector completo vía addFavoritePerformer / removeFavoritePerformer).
     */
    suspend fun getFavoritePerformers(collectorId: Int): List<Performer> {
        return api.getCollectorFavoritePerformers(collectorId)
    }

    /**
     * Agrega un artista como favorito del coleccionista.
     * Llama al endpoint correcto según si es músico o banda y actualiza la caché local.
     */
    suspend fun addFavoritePerformer(collectorId: Int, performer: Performer): Performer {
        val added = if (performer.isMusician) {
            api.addFavoriteMusician(collectorId, performer.id)
        } else {
            api.addFavoriteBand(collectorId, performer.id)
        }
        // Actualiza la caché local del coleccionista para reflejar el nuevo favorito
        val cached = dao.findById(collectorId)
        if (cached != null) {
            val updatedFavorites = cached.favoritePerformers + added
            dao.insertAll(listOf(cached.copy(favoritePerformers = updatedFavorites)))
        }
        return added
    }

    /**
     * Elimina un artista de los favoritos del coleccionista.
     * Llama al endpoint correcto según tipo y actualiza la caché local.
     */
    suspend fun removeFavoritePerformer(collectorId: Int, performer: Performer) {
        if (performer.isMusician) {
            api.removeFavoriteMusician(collectorId, performer.id)
        } else {
            api.removeFavoriteBand(collectorId, performer.id)
        }
        // Actualiza la caché local del coleccionista eliminando el favorito
        val cached = dao.findById(collectorId)
        if (cached != null) {
            val updatedFavorites = cached.favoritePerformers.filter { it.id != performer.id }
            dao.insertAll(listOf(cached.copy(favoritePerformers = updatedFavorites)))
        }
    }

    private suspend fun fetchAndCache(
        preservedFavorites: Map<Int, com.uniandes.vinilos.database.entities.CollectorEntity> = emptyMap()
    ): List<Collector> {
        val collectors = api.getCollectors()
        dao.insertAll(collectors.map { collector ->
            val entity = collector.toEntity()
            val preserved = preservedFavorites[collector.id]?.favoritePerformers ?: emptyList()
            if (entity.favoritePerformers.isEmpty() && preserved.isNotEmpty())
                entity.copy(favoritePerformers = preserved)
            else entity
        })
        return collectors.map { collector ->
            val preserved = preservedFavorites[collector.id]?.favoritePerformers ?: emptyList()
            if (collector.favoritePerformers.isEmpty() && preserved.isNotEmpty())
                collector.copy(favoritePerformers = preserved)
            else collector
        }
    }
}

