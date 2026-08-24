package com.example.comarleyaetheraudio.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {

    companion object {
        val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
        val ACCENT_COLOR_KEY = intPreferencesKey("accent_color")
    }

    val isDarkMode: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[DARK_MODE_KEY] ?: true // Modo oscuro por defecto
    }

    val accentColorIndex: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[ACCENT_COLOR_KEY] ?: 0 // Color por defecto (Azul/Cian SoundFlow)
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[DARK_MODE_KEY] = enabled }
    }

    suspend fun setAccentColor(colorIndex: Int) {
        context.dataStore.edit { prefs -> prefs[ACCENT_COLOR_KEY] = colorIndex }
    }
    private val LAST_VERSION_KEY = booleanPreferencesKey("v2_0_0_shown")

    val showChangelog: Flow<Boolean> = context.dataStore.data
        .map { prefs -> !(prefs[LAST_VERSION_KEY] ?: false) }

    suspend fun markChangelogAsShown() {
        context.dataStore.edit { prefs ->
            prefs[LAST_VERSION_KEY] = true
        }
    }
}