package com.example.myapplication.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.myapplication.data.UserPreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ThemeViewModel(application: Application) : AndroidViewModel(application) {
    private val preferencesManager = UserPreferencesManager(application)
    
    private val _isDarkTheme = MutableStateFlow(preferencesManager.isDarkThemeEnabled)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    fun updateTheme(isDark: Boolean) {
        preferencesManager.isDarkThemeEnabled = isDark
        _isDarkTheme.value = isDark
    }
} 