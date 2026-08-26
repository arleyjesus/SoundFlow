package com.example.comarleyaetheraudio.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.comarleyaetheraudio.ui.theme.AppTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "soundflow_settings")

class SettingsRepository(private val context: Context) {

    private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
    private val SHOW_CHANGELOG_KEY = booleanPreferencesKey("show_changelog_v220")
    private val APP_THEME_KEY = stringPreferencesKey("app_theme_style")

    val isDarkMode: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[DARK_MODE_KEY] ?: true
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[DARK_MODE_KEY] = enabled
        }
    }

    val showChangelog: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[SHOW_CHANGELOG_KEY] ?: true
    }

    suspend fun markChangelogAsShown() {
        context.dataStore.edit { prefs ->
            prefs[SHOW_CHANGELOG_KEY] = false
        }
    }

    val selectedTheme: Flow<AppTheme> = context.dataStore.data.map { prefs ->
        when (prefs[APP_THEME_KEY]) {
            "CLASSIC" -> AppTheme.CLASSIC
            "COOL" -> AppTheme.COOL
            "SIMPLE" -> AppTheme.SIMPLE
            else -> AppTheme.PRINCIPAL
        }
    }

    suspend fun setAppThemeStyle(theme: AppTheme) {
        context.dataStore.edit { prefs ->
            prefs[APP_THEME_KEY] = theme.name
        }
    }
}