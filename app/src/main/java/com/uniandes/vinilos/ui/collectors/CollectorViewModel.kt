package com.uniandes.vinilos.ui.collectors

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.uniandes.vinilos.database.VinilosDatabase
import com.uniandes.vinilos.model.Collector
import com.uniandes.vinilos.repository.CollectorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class CollectorViewModel(
    private val repository: CollectorRepository
) : ViewModel() {

    private val _collectors = MutableStateFlow<List<Collector>>(emptyList())
    val collectors: StateFlow<List<Collector>> = _collectors

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val pageSize = 4
    private val _visibleCount = MutableStateFlow(pageSize)

    // stateIn convierte el Flow derivado en StateFlow caliente: comparte una sola
    // suscripción entre todos los `collectAsStateWithLifecycle` y mantiene en memoria
    // el último valor para que las recomposiciones no disparen recolecciones nuevas.
    val visibleCollectors: StateFlow<List<Collector>> =
        combine(_collectors, _visibleCount) { list, count -> list.take(count) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000L),
                initialValue = emptyList()
            )

    val hasMore: StateFlow<Boolean> =
        combine(_collectors, _visibleCount) { list, count -> list.size > count }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000L),
                initialValue = false
            )

    fun loadMore() {
        _visibleCount.value += pageSize
    }

    init { loadCollectors() }

    fun loadCollectors() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                _collectors.value = repository.getCollectors()
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

    fun refreshCollectors() {
        viewModelScope.launch {
            _isRefreshing.value = true
            _error.value = null
            try {
                _collectors.value = repository.refreshCollectors()
            } catch (e: IOException) {
                _error.value = "Sin conexión. Revisa tu red e inténtalo de nuevo."
            } catch (e: HttpException) {
                _error.value = "El servidor respondió con un error (${e.code()})."
            } catch (e: Exception) {
                _error.value = "Error inesperado: ${e.message}"
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun loadCollector(id: Int) {
        if (_collectors.value.any { it.id == id }) return
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val collector = repository.getCollector(id)
                if (collector != null) {
                    _collectors.value = _collectors.value + collector
                }
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
     * Re-lee el coleccionista desde la caché Room (fuente de verdad) y reemplaza la
     * entrada en memoria. Se llama silenciosamente al volver a la pantalla de detalle
     * para reflejar cambios en los favoritos sin necesidad de llamar a la red.
     */
    fun refreshCollectorFromCache(id: Int) {
        viewModelScope.launch {
            try {
                val collector = repository.getCollector(id)
                if (collector != null) {
                    _collectors.value = _collectors.value
                        .filterNot { it.id == id } + collector
                }
            } catch (_: Exception) {
                // Actualización silenciosa de fondo; no sobreescribir errores visibles
            }
        }
    }

    fun findById(collectorId: Int): Collector? =
        _collectors.value.find { it.id == collectorId }

    companion object {
        fun factory(context: Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val dao = VinilosDatabase.getDatabase(context).collectorDao()
                    val repository = CollectorRepository(dao)
                    @Suppress("UNCHECKED_CAST")
                    return CollectorViewModel(repository) as T
                }
            }
    }
}
