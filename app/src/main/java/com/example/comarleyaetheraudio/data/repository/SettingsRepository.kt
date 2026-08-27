package com.example.comarleyaetheraudio.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.comarleyaetheraudio.ui.theme.AppTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings_prefs")

class SettingsRepository(private val context: Context) {

    private val IS_DARK_MODE_KEY = booleanPreferencesKey("is_dark_mode")
    private val SELECTED_THEME_KEY = stringPreferencesKey("selected_theme_style")

    // Escucha del Modo Oscuro
    val isDarkMode: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[IS_DARK_MODE_KEY] ?: true
    }

    // Escucha del Tema Dinámico (Principal, Clásico, Cool, Minimalista)
    val selectedTheme: Flow<AppTheme> = context.dataStore.data.map { prefs ->
        val themeName = prefs[SELECTED_THEME_KEY] ?: AppTheme.PRINCIPAL.name
        try {
            AppTheme.valueOf(themeName)
        } catch (_: Exception) {
            AppTheme.PRINCIPAL
        }
    }

    // Guardar preferencia de Modo Oscuro
    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[IS_DARK_MODE_KEY] = enabled
        }
    }

    // Guardar preferencia de Tema Dinámico
    suspend fun setSelectedTheme(theme: AppTheme) {
        context.dataStore.edit { prefs ->
            prefs[SELECTED_THEME_KEY] = theme.name
        }
    }
}