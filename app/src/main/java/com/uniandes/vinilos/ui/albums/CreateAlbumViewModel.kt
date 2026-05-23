package com.uniandes.vinilos.ui.albums

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.uniandes.vinilos.database.VinilosDatabase
import com.uniandes.vinilos.model.Album
import com.uniandes.vinilos.model.CreateAlbumRequest
import com.uniandes.vinilos.repository.AlbumRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class CreateAlbumUiState {
    object Idle : CreateAlbumUiState()
    object Loading : CreateAlbumUiState()
    data class Success(val album: Album) : CreateAlbumUiState()
    data class Error(val message: String) : CreateAlbumUiState()
}

class CreateAlbumViewModel(
    private val repository: AlbumRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<CreateAlbumUiState>(CreateAlbumUiState.Idle)
    val uiState: StateFlow<CreateAlbumUiState> = _uiState.asStateFlow()

    // Form fields
    val name = MutableStateFlow("")
    val cover = MutableStateFlow("")
    val releaseDate = MutableStateFlow("")
    val description = MutableStateFlow("")
    val genre = MutableStateFlow("")
    val recordLabel = MutableStateFlow("")

    // Validation errors
    private val _nameError = MutableStateFlow<String?>(null)
    val nameError: StateFlow<String?> = _nameError.asStateFlow()

    private val _releaseDateError = MutableStateFlow<String?>(null)
    val releaseDateError: StateFlow<String?> = _releaseDateError.asStateFlow()

    private val _genreError = MutableStateFlow<String?>(null)
    val genreError: StateFlow<String?> = _genreError.asStateFlow()

    private val _recordLabelError = MutableStateFlow<String?>(null)
    val recordLabelError: StateFlow<String?> = _recordLabelError.asStateFlow()

    private val _descriptionError = MutableStateFlow<String?>(null)
    val descriptionError: StateFlow<String?> = _descriptionError.asStateFlow()

    fun submitAlbum() {
        if (!validate()) return

        viewModelScope.launch {
            _uiState.value = CreateAlbumUiState.Loading

            // El backend espera fecha ISO; construimos desde el año ingresado
            val year = releaseDate.value.trim()
            val isoDate = "$year-01-01T00:00:00.000Z"

            val request = CreateAlbumRequest(
                name = name.value.trim(),
                cover = cover.value.trim().ifEmpty { "https://via.placeholder.com/300x300.png?text=Vinilos" },
                releaseDate = isoDate,
                description = description.value.trim(),
                genre = genre.value,
                recordLabel = recordLabel.value
            )

            val result = repository.createAlbum(request)
            _uiState.value = if (result.isSuccess) {
                CreateAlbumUiState.Success(result.getOrThrow())
            } else {
                CreateAlbumUiState.Error(
                    result.exceptionOrNull()?.message ?: "Error al crear el álbum"
                )
            }
        }
    }

    fun resetState() {
        _uiState.value = CreateAlbumUiState.Idle
    }

    private fun validate(): Boolean {
        var isValid = true

        _nameError.value = if (name.value.isBlank()) {
            isValid = false
            "El título es obligatorio"
        } else null

        val year = releaseDate.value.trim().toIntOrNull()
        _releaseDateError.value = when {
            releaseDate.value.isBlank() -> { isValid = false; "El año es obligatorio" }
            year == null -> { isValid = false; "Ingresa un año válido" }
            year < 1900 || year > 2025 -> { isValid = false; "El año debe estar entre 1900 y 2025" }
            else -> null
        }

        _genreError.value = if (genre.value.isBlank()) {
            isValid = false
            "Selecciona un género"
        } else null

        _recordLabelError.value = if (recordLabel.value.isBlank()) {
            isValid = false
            "Selecciona un sello discográfico"
        } else null

        _descriptionError.value = if (description.value.isBlank()) {
            isValid = false
            "La descripción es obligatoria"
        } else null

        return isValid
    }

    companion object {
        fun factory(context: Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val dao = VinilosDatabase.getDatabase(context).albumDao()
                    return CreateAlbumViewModel(AlbumRepository(dao)) as T
                }
            }
    }
}
