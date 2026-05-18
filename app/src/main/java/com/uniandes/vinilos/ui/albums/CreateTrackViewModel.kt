package com.uniandes.vinilos.ui.albums

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.uniandes.vinilos.database.VinilosDatabase
import com.uniandes.vinilos.model.CreateTrackRequest
import com.uniandes.vinilos.model.Track
import com.uniandes.vinilos.repository.AlbumRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class CreateTrackUiState {
    object Idle : CreateTrackUiState()
    object Loading : CreateTrackUiState()
    data class Success(val track: Track) : CreateTrackUiState()
    data class Error(val message: String) : CreateTrackUiState()
}

class CreateTrackViewModel(
    private val repository: AlbumRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<CreateTrackUiState>(CreateTrackUiState.Idle)
    val uiState: StateFlow<CreateTrackUiState> = _uiState.asStateFlow()

    val name = MutableStateFlow("")
    val duration = MutableStateFlow("")

    private val _nameError = MutableStateFlow<String?>(null)
    val nameError: StateFlow<String?> = _nameError.asStateFlow()

    private val _durationError = MutableStateFlow<String?>(null)
    val durationError: StateFlow<String?> = _durationError.asStateFlow()

    fun submitTrack(albumId: Int) {
        if (!validate()) return

        viewModelScope.launch {
            _uiState.value = CreateTrackUiState.Loading

            val request = CreateTrackRequest(
                name = name.value.trim(),
                duration = duration.value.trim()
            )

            val result = repository.addTrackToAlbum(albumId, request)
            _uiState.value = if (result.isSuccess) {
                CreateTrackUiState.Success(result.getOrThrow())
            } else {
                CreateTrackUiState.Error(
                    result.exceptionOrNull()?.message ?: "Error al asociar la canción"
                )
            }
        }
    }

    fun resetState() {
        _uiState.value = CreateTrackUiState.Idle
        name.value = ""
        duration.value = ""
        _nameError.value = null
        _durationError.value = null
    }

    private fun validate(): Boolean {
        var isValid = true

        _nameError.value = when {
            name.value.isBlank() -> {
                isValid = false
                "El nombre de la canción es obligatorio"
            }
            name.value.trim().length > 80 -> {
                isValid = false
                "El nombre no puede superar 80 caracteres"
            }
            else -> null
        }

        _durationError.value = when {
            duration.value.isBlank() -> {
                isValid = false
                "La duración es obligatoria"
            }
            !DURATION_REGEX.matches(duration.value.trim()) -> {
                isValid = false
                "Formato esperado mm:ss (ej. 3:45)"
            }
            else -> null
        }

        return isValid
    }

    companion object {
        // Acepta m:ss o mm:ss, segundos siempre 00..59. Ej: 3:45, 12:08, 0:30.
        private val DURATION_REGEX = Regex("^([0-9]|[1-9][0-9]):[0-5][0-9]$")

        fun factory(context: Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val dao = VinilosDatabase.getDatabase(context).albumDao()
                    return CreateTrackViewModel(AlbumRepository(dao)) as T
                }
            }
    }
}
