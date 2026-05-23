package com.uniandes.vinilos.repository

import com.uniandes.vinilos.database.dao.AlbumDao
import com.uniandes.vinilos.database.toAlbum
import com.uniandes.vinilos.database.toEntity
import com.uniandes.vinilos.model.Album
import com.uniandes.vinilos.model.CreateAlbumRequest
import com.uniandes.vinilos.model.CreateTrackRequest
import com.uniandes.vinilos.model.Track
import com.uniandes.vinilos.network.NetworkServiceAdapter
import com.uniandes.vinilos.network.VinilosApi

class AlbumRepository(
    private val dao: AlbumDao,
    private val api: VinilosApi = NetworkServiceAdapter.api
) {

    suspend fun getAlbums(): List<Album> {
        val cached = dao.getAll()
        if (cached.isNotEmpty()) {
            return cached.map { it.toAlbum() }
        }
        return fetchAndCache()
    }

    suspend fun refreshAlbums(): List<Album> {
        dao.deleteAll()
        return fetchAndCache()
    }
 
    suspend fun createAlbum(request: CreateAlbumRequest): Result<Album> {
        return try {
            val created = api.createAlbum(request)
            dao.deleteAll()
            Result.success(created)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addTrackToAlbum(albumId: Int, request: CreateTrackRequest): Result<Track> {
        return try {
            val created = api.addTrackToAlbum(albumId, request)
            // Invalidamos la caché para que la próxima lectura traiga el álbum
            // con el nuevo track incluido en su lista.
            dao.deleteAll()
            Result.success(created)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun fetchAndCache(): List<Album> {
        val albums = api.getAlbums().map { it.copy(releaseDate = it.releaseDate.take(4)) }
        dao.insertAll(albums.map { it.toEntity() })
        return albums
    }
}
