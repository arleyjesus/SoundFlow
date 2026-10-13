package com.example.comarleyaetheraudio.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.comarleyaetheraudio.ui.theme.AppTheme
import com.example.comarleyaetheraudio.ui.theme.AppearanceSettings
import com.example.comarleyaetheraudio.domain.model.NetworkSettings
import com.example.comarleyaetheraudio.domain.model.NetworkFeature
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.distinctUntilChanged

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings_prefs")
private val Context.listeningStore: DataStore<Preferences> by preferencesDataStore(name = "listening_private")

class SettingsRepository(private val store: DataStore<Preferences>, private val historyStore: DataStore<Preferences> = store) {
    constructor(context: Context) : this(context.applicationContext.dataStore, context.applicationContext.listeningStore)

    private val IS_DARK_MODE_KEY = booleanPreferencesKey("is_dark_mode")
    private val SELECTED_THEME_KEY = stringPreferencesKey("selected_theme_style")
    private val ANIMATIONS_KEY = booleanPreferencesKey("appearance_animations")
    private val TRANSPARENCIES_KEY = booleanPreferencesKey("appearance_transparencies")
    private val FOLLOW_SYSTEM_KEY = booleanPreferencesKey("appearance_follow_system")
    private val LIBRARY_ORDER_KEY = stringPreferencesKey("library_order")
    private val BACKGROUND_KEY = stringPreferencesKey("appearance_background_uri")
    private val LISTENING_ENABLED = booleanPreferencesKey("listening_history_enabled")
    private val LISTENING_COUNTS = stringPreferencesKey("listening_history_counts")
    val listeningEnabled: Flow<Boolean> = historyStore.data.map { it[LISTENING_ENABLED] ?: false }.distinctUntilChanged()
    val listeningCounts: Flow<Map<Long, Int>> = historyStore.data.map {
        com.example.comarleyaetheraudio.domain.library.decodeListeningCounts(it[LISTENING_COUNTS])
    }
    suspend fun setListeningEnabled(enabled: Boolean) { historyStore.edit { it[LISTENING_ENABLED] = enabled } }
    suspend fun clearListeningCounts() { historyStore.edit { it.remove(LISTENING_COUNTS) } }
    suspend fun recordListening(id: Long) { historyStore.edit {
        if (it[LISTENING_ENABLED] != true) return@edit
        val counts = com.example.comarleyaetheraudio.domain.library.decodeListeningCounts(it[LISTENING_COUNTS]).toMutableMap()
        counts[id] = ((counts[id] ?: 0) + 1).coerceAtMost(1_000_000)
        it[LISTENING_COUNTS] = com.example.comarleyaetheraudio.domain.library.encodeListeningCounts(counts)
    } }
    suspend fun setBackground(uri: String?) {
        store.edit { if (uri == null) it.remove(BACKGROUND_KEY) else it[BACKGROUND_KEY] = uri }
    }
    val libraryOrder: Flow<com.example.comarleyaetheraudio.domain.library.LibraryOrder> = store.data.map { prefs ->
        com.example.comarleyaetheraudio.domain.library.LibraryOrder.entries.firstOrNull { it.name == prefs[LIBRARY_ORDER_KEY] }
            ?: com.example.comarleyaetheraudio.domain.library.LibraryOrder.ADDED_NEWEST
    }
    suspend fun setLibraryOrder(order: com.example.comarleyaetheraudio.domain.library.LibraryOrder) {
        store.edit { it[LIBRARY_ORDER_KEY] = order.name }
    }
    private fun networkKey(feature: NetworkFeature) = booleanPreferencesKey("network_${feature.name.lowercase(java.util.Locale.ROOT)}")

    val networkSettings: Flow<NetworkSettings> = store.data.map { prefs ->
        NetworkSettings(
            artistImages = prefs[networkKey(NetworkFeature.ARTIST_IMAGES)] ?: true,
            lyrics = prefs[networkKey(NetworkFeature.LYRICS)] ?: true,
            updates = prefs[networkKey(NetworkFeature.UPDATES)] ?: true
        )
    }

    suspend fun setNetworkFeature(feature: NetworkFeature, enabled: Boolean) {
        store.edit { it[networkKey(feature)] = enabled }
    }

    val appearance: Flow<AppearanceSettings> = store.data.map { prefs ->
        AppearanceSettings(
            isDarkMode = prefs[IS_DARK_MODE_KEY] ?: true,
            theme = AppTheme.entries.firstOrNull { it.name == prefs[SELECTED_THEME_KEY] } ?: AppTheme.PRINCIPAL,
            animationsEnabled = prefs[ANIMATIONS_KEY] ?: true,
            transparenciesEnabled = prefs[TRANSPARENCIES_KEY] ?: true,
            followSystemTheme = prefs[FOLLOW_SYSTEM_KEY] ?: false,
            backgroundUri = prefs[BACKGROUND_KEY]
        )
    }

    suspend fun setAnimationsEnabled(enabled: Boolean) {
        store.edit { it[ANIMATIONS_KEY] = enabled }
    }

    suspend fun setTransparenciesEnabled(enabled: Boolean) {
        store.edit { it[TRANSPARENCIES_KEY] = enabled }
    }

    suspend fun setFollowSystemTheme(enabled: Boolean) {
        store.edit { it[FOLLOW_SYSTEM_KEY] = enabled }
    }

    suspend fun resetAppearance() {
        store.edit {
            it.remove(IS_DARK_MODE_KEY)
            it.remove(SELECTED_THEME_KEY)
            it.remove(ANIMATIONS_KEY)
            it.remove(TRANSPARENCIES_KEY)
            it.remove(FOLLOW_SYSTEM_KEY)
            it.remove(BACKGROUND_KEY)
        }
    }

    // Escucha del Modo Oscuro
    val isDarkMode: Flow<Boolean> = store.data.map { prefs ->
        prefs[IS_DARK_MODE_KEY] ?: true
    }

    // Escucha del Tema Dinámico (Principal, Clásico, Cool, Minimalista)
    val selectedTheme: Flow<AppTheme> = store.data.map { prefs ->
        val themeName = prefs[SELECTED_THEME_KEY] ?: AppTheme.PRINCIPAL.name
        try {
            AppTheme.valueOf(themeName)
        } catch (_: Exception) {
            AppTheme.PRINCIPAL
        }
    }

    // Guardar preferencia de Modo Oscuro
    suspend fun setDarkMode(enabled: Boolean) {
        store.edit { prefs ->
            prefs[IS_DARK_MODE_KEY] = enabled
            prefs[FOLLOW_SYSTEM_KEY] = false
        }
    }

    // Guardar preferencia de Tema Dinámico
    suspend fun setSelectedTheme(theme: AppTheme) {
        store.edit { prefs ->
            prefs[SELECTED_THEME_KEY] = theme.name
        }
    }
}
