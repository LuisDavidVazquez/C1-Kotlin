package com.example.myapplication.viewmodel

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.UserPreferencesManager
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*

data class UserPreferencesState(
    val username: String = "",
    val isDarkThemeEnabled: Boolean = false,
    val preferredLanguage: String = "es",
    val notificationVolume: Float = 1.0f,
    val lastAccess: String = "",
    val lastLocation: String = "",
    val totalUsageTime: String = "0h 0m",
    val isUpdatingLocation: Boolean = false
)

class UserPreferencesViewModel(application: Application) : AndroidViewModel(application) {
    private val preferencesManager = UserPreferencesManager(application)
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    private var usageTrackingJob: Job? = null
    private var startTime: Long = 0L

    private val _uiState = MutableStateFlow(UserPreferencesState())
    val uiState: StateFlow<UserPreferencesState> = _uiState.asStateFlow()

    init {
        loadPreferences()
    }

    private fun loadPreferences() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                username = preferencesManager.username,
                isDarkThemeEnabled = preferencesManager.isDarkThemeEnabled,
                preferredLanguage = preferencesManager.preferredLanguage,
                notificationVolume = preferencesManager.notificationVolume,
                lastAccess = formatDate(preferencesManager.lastAccess),
                lastLocation = preferencesManager.lastLocation,
                totalUsageTime = formatUsageTime(preferencesManager.totalUsageTime)
            )
        }
    }

    fun startUsageTracking() {
        startTime = System.currentTimeMillis()
        usageTrackingJob = viewModelScope.launch {
            while (isActive) {
                delay(1000) // Actualizar cada segundo
                val currentTime = System.currentTimeMillis()
                val sessionTime = currentTime - startTime
                preferencesManager.addUsageTime(1000) // Agregar un segundo
                loadPreferences()
            }
        }
    }

    fun stopUsageTracking() {
        usageTrackingJob?.cancel()
        usageTrackingJob = null
    }

    fun requestLocationUpdate(context: Context) {
        val locationClient = LocationServices.getFusedLocationProviderClient(context)
        
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        // Actualizar estado a "actualizando"
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUpdatingLocation = true)
        }

        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            numUpdates = 1
        }

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    viewModelScope.launch {
                        try {
                            val geocoder = Geocoder(context, Locale.getDefault())
                            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                            
                            if (!addresses.isNullOrEmpty()) {
                                val address = addresses[0]
                                val locationString = buildString {
                                    append(address.locality ?: "")
                                    if (address.locality != null && address.adminArea != null) append(", ")
                                    append(address.adminArea ?: "")
                                }
                                preferencesManager.lastLocation = locationString
                            } else {
                                preferencesManager.lastLocation = "${location.latitude}, ${location.longitude}"
                            }
                        } catch (e: Exception) {
                            preferencesManager.lastLocation = "${location.latitude}, ${location.longitude}"
                        } finally {
                            loadPreferences()
                            _uiState.value = _uiState.value.copy(isUpdatingLocation = false)
                        }
                    }
                }
                locationClient.removeLocationUpdates(this)
            }
        }

        locationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        ).addOnFailureListener {
            viewModelScope.launch {
                _uiState.value = _uiState.value.copy(isUpdatingLocation = false)
            }
        }
    }

    fun updateUsername(username: String) {
        preferencesManager.username = username
        loadPreferences()
    }

    fun toggleDarkTheme() {
        preferencesManager.isDarkThemeEnabled = !preferencesManager.isDarkThemeEnabled
        loadPreferences()
    }

    fun updatePreferredLanguage(language: String) {
        preferencesManager.preferredLanguage = language
        loadPreferences()
    }

    fun updateNotificationVolume(volume: Float) {
        preferencesManager.notificationVolume = volume
        loadPreferences()
    }

    fun updateLastAccess() {
        preferencesManager.updateLastAccess()
        loadPreferences()
    }

    private fun formatDate(timestamp: Long): String {
        return if (timestamp == 0L) {
            "Nunca"
        } else {
            dateFormat.format(Date(timestamp))
        }
    }

    private fun formatUsageTime(timeInMillis: Long): String {
        val hours = timeInMillis / (1000 * 60 * 60)
        val minutes = (timeInMillis % (1000 * 60 * 60)) / (1000 * 60)
        val seconds = (timeInMillis % (1000 * 60)) / 1000
        return "${hours}h ${minutes}m ${seconds}s"
    }

    override fun onCleared() {
        super.onCleared()
        stopUsageTracking()
    }
} 