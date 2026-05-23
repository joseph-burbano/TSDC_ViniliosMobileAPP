package com.uniandes.vinilos.ui.prizes

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.uniandes.vinilos.database.VinilosDatabase
import com.uniandes.vinilos.model.PerformerPrize
import com.uniandes.vinilos.model.Prize
import com.uniandes.vinilos.repository.PrizeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class PrizeViewModel(
    private val repository: PrizeRepository
) : ViewModel() {

    private val _prizes = MutableStateFlow<List<Prize>>(emptyList())
    val prizes: StateFlow<List<Prize>> = _prizes.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _associationSuccess = MutableStateFlow<PerformerPrize?>(null)
    val associationSuccess: StateFlow<PerformerPrize?> = _associationSuccess.asStateFlow()

    init { loadPrizes() }

    fun loadPrizes() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                _prizes.value = repository.getPrizes()
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

    fun refreshPrizes() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                _prizes.value = repository.refreshPrizes()
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
     * Submits the full associate flow: optionally creates a new prize, then links it
     * to the given performer with the supplied premiation date. The UI consumes
     * `associationSuccess` to navigate away once the round-trip lands.
     */
    fun submitAssociation(
        performerId: Int,
        isMusician: Boolean,
        premiationDate: String,
        selectedPrizeId: Int?,
        newPrizeName: String?,
        newPrizeDescription: String?,
        newPrizeOrganization: String?
    ) {
        if (premiationDate.isBlank()) {
            _error.value = "La fecha de premiación es obligatoria."
            return
        }
        if (selectedPrizeId == null && newPrizeName.isNullOrBlank()) {
            _error.value = "Selecciona un premio existente o ingresa el nombre del nuevo premio."
            return
        }

        viewModelScope.launch {
            _isSubmitting.value = true
            _error.value = null
            try {
                val prizeId = selectedPrizeId ?: repository.createPrize(
                    name = newPrizeName!!.trim(),
                    description = newPrizeDescription.orEmpty().trim(),
                    organization = newPrizeOrganization.orEmpty().trim()
                ).also { created ->
                    // Push the freshly-created prize into the visible list so the UI
                    // reflects it without an extra refresh.
                    _prizes.value = _prizes.value + created
                }.id

                _associationSuccess.value = repository.associatePrizeToPerformer(
                    prizeId = prizeId,
                    performerId = performerId,
                    isMusician = isMusician,
                    premiationDate = premiationDate
                )
            } catch (e: IOException) {
                _error.value = "Sin conexión. Revisa tu red e inténtalo de nuevo."
            } catch (e: HttpException) {
                _error.value = "El servidor respondió con un error (${e.code()})."
            } catch (e: Exception) {
                _error.value = "Error inesperado: ${e.message}"
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    fun consumeAssociationSuccess() {
        _associationSuccess.value = null
    }

    fun clearError() {
        _error.value = null
    }

    companion object {
        fun factory(context: Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val dao = VinilosDatabase.getDatabase(context).prizeDao()
                    val repository = PrizeRepository(dao)
                    @Suppress("UNCHECKED_CAST")
                    return PrizeViewModel(repository) as T
                }
            }
    }
}
