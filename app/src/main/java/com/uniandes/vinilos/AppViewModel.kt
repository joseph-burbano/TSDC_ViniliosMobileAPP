package com.uniandes.vinilos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uniandes.vinilos.model.ColorBlindMode
import com.uniandes.vinilos.model.UserRole
import com.uniandes.vinilos.repository.IPreferencesRepository
import com.uniandes.vinilos.repository.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow

@HiltViewModel
class AppViewModel @Inject constructor(
    private val preferencesRepository: IPreferencesRepository
) : ViewModel() {

    val userRole: StateFlow<UserRole?> = preferencesRepository.userRole
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    val isDarkTheme: StateFlow<Boolean> = preferencesRepository.isDarkTheme
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = false
        )

    val isReady: StateFlow<Boolean> = preferencesRepository.userRole
        .map { true }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = false
        ) 

    fun setUserRole(role: UserRole) {
        viewModelScope.launch {
            preferencesRepository.setUserRole(role)
        }
    }

    fun clearUserRole() {
        viewModelScope.launch {
            preferencesRepository.clearUserRole()
        }
    }

    fun setDarkTheme(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setDarkTheme(enabled)
        }
    }

    fun toggleDarkTheme() {
        viewModelScope.launch {
            preferencesRepository.setDarkTheme(!isDarkTheme.value)
        }
    }

    val colorBlindMode: StateFlow<ColorBlindMode> = preferencesRepository.colorBlindMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = ColorBlindMode.NONE
        )

    fun setColorBlindMode(mode: ColorBlindMode) {
        viewModelScope.launch {
            preferencesRepository.setColorBlindMode(mode)
        }
    }
}
