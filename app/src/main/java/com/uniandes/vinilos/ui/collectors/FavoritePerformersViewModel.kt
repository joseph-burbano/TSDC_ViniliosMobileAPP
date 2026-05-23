package com.uniandes.vinilos.ui.collectors

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.uniandes.vinilos.database.VinilosDatabase
import com.uniandes.vinilos.model.Performer
import com.uniandes.vinilos.repository.ArtistRepository
import com.uniandes.vinilos.repository.CollectorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class FavoritePerformersViewModel(
    private val collectorRepository: CollectorRepository,
    private val artistRepository: ArtistRepository
) : ViewModel() {

    /** Lista completa de artistas disponibles (músicos + bandas). */
    private val _allPerformers = MutableStateFlow<List<Performer>>(emptyList())
    val allPerformers: StateFlow<List<Performer>> = _allPerformers.asStateFlow()

    /** IDs de los artistas que ya son favoritos del coleccionista activo. */
    private val _favoriteIds = MutableStateFlow<Set<Int>>(emptySet())
    val favoriteIds: StateFlow<Set<Int>> = _favoriteIds.asStateFlow()

    /** true mientras se carga la lista inicial de artistas. */
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    /**
     * ID del artista cuyo toggle está en curso (para mostrar un indicador por ítem).
     * null cuando ningún toggle está en vuelo.
     */
    private val _isTogglingId = MutableStateFlow<Int?>(null)
    val isTogglingId: StateFlow<Int?> = _isTogglingId.asStateFlow()

    /** Mensaje de error para mostrar al usuario, null cuando no hay error. */
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    /**
     * Carga la lista de todos los artistas disponibles y establece el estado inicial
     * de favoritos a partir de la lista ya conocida del coleccionista.
     *
     * @param collectorId       ID del coleccionista activo.
     * @param initialFavorites  Lista de favoritos actuales (viene del Collector ya cargado).
     */
    fun loadData(collectorId: Int, initialFavorites: List<Performer>) {
        _favoriteIds.value = initialFavorites.map { it.id }.toSet()
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                _allPerformers.value = artistRepository.getPerformers()
            } catch (e: IOException) {
                _error.value = "Sin conexión. Revisa tu red e inténtalo de nuevo."
            } catch (e: HttpException) {
                _error.value = "El servidor respondió con un error (${e.code()})."
            } catch (e: Exception) {
                _error.value = "Error inesperado: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Alterna el estado de favorito de un artista con UI optimista:
     * actualiza la UI de inmediato y revierte si el servidor falla.
     */
    fun toggleFavorite(collectorId: Int, performer: Performer) {
        val wasFavorite = _favoriteIds.value.contains(performer.id)
        viewModelScope.launch {
            _isTogglingId.value = performer.id
            _error.value = null

            // Actualización optimista
            _favoriteIds.value = if (wasFavorite) {
                _favoriteIds.value - performer.id
            } else {
                _favoriteIds.value + performer.id
            }

            try {
                if (wasFavorite) {
                    collectorRepository.removeFavoritePerformer(collectorId, performer)
                } else {
                    collectorRepository.addFavoritePerformer(collectorId, performer)
                }
            } catch (e: IOException) {
                // Revertir actualización optimista
                _favoriteIds.value = if (wasFavorite) {
                    _favoriteIds.value + performer.id
                } else {
                    _favoriteIds.value - performer.id
                }
                _error.value = "Sin conexión. Revisa tu red e inténtalo de nuevo."
            } catch (e: HttpException) {
                _favoriteIds.value = if (wasFavorite) {
                    _favoriteIds.value + performer.id
                } else {
                    _favoriteIds.value - performer.id
                }
                _error.value = "El servidor respondió con un error (${e.code()})."
            } catch (e: Exception) {
                _favoriteIds.value = if (wasFavorite) {
                    _favoriteIds.value + performer.id
                } else {
                    _favoriteIds.value - performer.id
                }
                _error.value = "Error inesperado: ${e.message}"
            } finally {
                _isTogglingId.value = null
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    companion object {
        fun factory(context: Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val db = VinilosDatabase.getDatabase(context)
                    val collectorRepository = CollectorRepository(db.collectorDao())
                    val artistRepository = ArtistRepository(db.performerDao())
                    @Suppress("UNCHECKED_CAST")
                    return FavoritePerformersViewModel(collectorRepository, artistRepository) as T
                }
            }
    }
}
