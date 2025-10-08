package com.timebloom.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timebloom.app.data.preferences.UserPreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val userPreferences: UserPreferences
) : ViewModel() {

    val themeMode: StateFlow<String> = userPreferences.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "auto")

    val gardenTheme: StateFlow<String> = userPreferences.gardenTheme
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "zen")

    val notificationsEnabled: StateFlow<Boolean> = userPreferences.notificationsEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            userPreferences.setThemeMode(mode)
        }
    }

    fun setGardenTheme(theme: String) {
        viewModelScope.launch {
            userPreferences.setGardenTheme(theme)
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setNotificationsEnabled(enabled)
        }
    }
}