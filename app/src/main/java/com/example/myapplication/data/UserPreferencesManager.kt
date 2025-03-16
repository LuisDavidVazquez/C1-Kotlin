package com.example.myapplication.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import java.util.Date

class UserPreferencesManager(context: Context) {
    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

    private val sharedPreferences = EncryptedSharedPreferences.create(
        "secure_user_prefs",
        masterKeyAlias,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    companion object {
        private const val KEY_USERNAME = "username"
        private const val KEY_DARK_THEME = "dark_theme"
        private const val KEY_PREFERRED_LANGUAGE = "preferred_language"
        private const val KEY_NOTIFICATION_VOLUME = "notification_volume"
        private const val KEY_LAST_ACCESS = "last_access"
        private const val KEY_LAST_LOCATION = "last_location"
        private const val KEY_TOTAL_USAGE_TIME = "total_usage_time"
    }

    var username: String
        get() = sharedPreferences.getString(KEY_USERNAME, "") ?: ""
        set(value) = sharedPreferences.edit().putString(KEY_USERNAME, value).apply()

    var isDarkThemeEnabled: Boolean
        get() = sharedPreferences.getBoolean(KEY_DARK_THEME, false)
        set(value) = sharedPreferences.edit().putBoolean(KEY_DARK_THEME, value).apply()

    var preferredLanguage: String
        get() = sharedPreferences.getString(KEY_PREFERRED_LANGUAGE, "es") ?: "es"
        set(value) = sharedPreferences.edit().putString(KEY_PREFERRED_LANGUAGE, value).apply()

    var notificationVolume: Float
        get() = sharedPreferences.getFloat(KEY_NOTIFICATION_VOLUME, 1.0f)
        set(value) = sharedPreferences.edit().putFloat(KEY_NOTIFICATION_VOLUME, value).apply()

    var lastAccess: Long
        get() = sharedPreferences.getLong(KEY_LAST_ACCESS, 0L)
        set(value) = sharedPreferences.edit().putLong(KEY_LAST_ACCESS, value).apply()

    var lastLocation: String
        get() = sharedPreferences.getString(KEY_LAST_LOCATION, "") ?: ""
        set(value) = sharedPreferences.edit().putString(KEY_LAST_LOCATION, value).apply()

    var totalUsageTime: Long
        get() = sharedPreferences.getLong(KEY_TOTAL_USAGE_TIME, 0L)
        set(value) = sharedPreferences.edit().putLong(KEY_TOTAL_USAGE_TIME, value).apply()

    fun updateLastAccess() {
        lastAccess = System.currentTimeMillis()
    }

    fun addUsageTime(timeInMillis: Long) {
        totalUsageTime += timeInMillis
    }

    fun clearAll() {
        sharedPreferences.edit().clear().apply()
    }

    fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
    }

    fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
    }
} 