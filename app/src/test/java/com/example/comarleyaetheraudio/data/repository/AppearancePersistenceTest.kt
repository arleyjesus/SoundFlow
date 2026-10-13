package com.example.comarleyaetheraudio.data.repository

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.comarleyaetheraudio.ui.theme.AppTheme
import com.example.comarleyaetheraudio.ui.theme.AppearanceSettings
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.nio.file.Files

class AppearancePersistenceTest {
    @Test
    fun legacyPreferencesKeepTheirPaletteAndDefaultEffects() = runBlocking {
        withStore { store ->
            store.edit {
                it[stringPreferencesKey("selected_theme_style")] = AppTheme.COOL.name
                it[androidx.datastore.preferences.core.booleanPreferencesKey("is_dark_mode")] = false
            }
            assertEquals(AppearanceSettings(false, AppTheme.COOL), SettingsRepository(store).appearance.first())
        }
    }

    @Test
    fun effectSwitchesAreIndependentAndPreserveUnrelatedSettings() = runBlocking {
        withStore { store ->
            val sentinel = stringPreferencesKey("unrelated_setting")
            store.edit { it[sentinel] = "preserve" }
            val repository = SettingsRepository(store)
            for (animations in listOf(false, true)) for (transparencies in listOf(false, true)) {
                repository.setAnimationsEnabled(animations)
                repository.setTransparenciesEnabled(transparencies)
                val settings = repository.appearance.first()
                assertEquals(animations, settings.animationsEnabled)
                assertEquals(transparencies, settings.transparenciesEnabled)
            }
            assertEquals("preserve", store.data.first()[sentinel])
        }
    }

    @Test
    fun resetOnlyRemovesAppearanceKeysAndIsIdempotent() = runBlocking {
        withStore { store ->
            val audio = stringPreferencesKey("audio_profile")
            val unrelated = stringPreferencesKey("unrelated_setting")
            store.edit { it[audio] = "custom"; it[unrelated] = "keep" }
            val repository = SettingsRepository(store)
            repository.setDarkMode(false)
            repository.setSelectedTheme(AppTheme.COOL)
            repository.setAnimationsEnabled(false)
            repository.setTransparenciesEnabled(false)
            repository.setFollowSystemTheme(true)
            repository.setLibraryOrder(com.example.comarleyaetheraudio.domain.library.LibraryOrder.ARTIST)
            repeat(2) { repository.resetAppearance() }
            assertEquals(AppearanceSettings(), repository.appearance.first())
            assertEquals("custom", store.data.first()[audio])
            assertEquals("keep", store.data.first()[unrelated])
            assertEquals(com.example.comarleyaetheraudio.domain.library.LibraryOrder.ARTIST, repository.libraryOrder.first())
        }
    }

    @Test
    fun choosingManualDarkModeStopsFollowingSystemWithoutChangingPaletteOrEffects() = runBlocking {
        withStore { store ->
            val repository = SettingsRepository(store)
            repository.setSelectedTheme(AppTheme.CLASSIC)
            repository.setAnimationsEnabled(false)
            repository.setFollowSystemTheme(true)
            assertTrue(repository.appearance.first().followSystemTheme)
            repository.setDarkMode(false)
            assertEquals(AppearanceSettings(false, AppTheme.CLASSIC, false, true), repository.appearance.first())
        }
    }

    @Test
    fun preferencesSurviveClosingAndReopeningTheStore() = runBlocking {
        val directory = Files.createTempDirectory("soundflow-appearance-").toFile()
        val file = File(directory, "isolated.preferences_pb")
        suspend fun open(action: suspend (SettingsRepository) -> Unit) {
            val job = SupervisorJob()
            val store = PreferenceDataStoreFactory.create(scope = CoroutineScope(job + Dispatchers.IO), produceFile = { file })
            try { action(SettingsRepository(store)) } finally { job.cancelAndJoin() }
        }
        try {
            open { repository ->
                repository.setDarkMode(false)
                repository.setSelectedTheme(AppTheme.CLASSIC)
                repository.setAnimationsEnabled(false)
                repository.setTransparenciesEnabled(false)
                repository.setFollowSystemTheme(true)
                repository.setLibraryOrder(com.example.comarleyaetheraudio.domain.library.LibraryOrder.TITLE)
            }
            open {
                assertEquals(AppearanceSettings(false, AppTheme.CLASSIC, false, false, followSystemTheme = true), it.appearance.first())
                assertEquals(com.example.comarleyaetheraudio.domain.library.LibraryOrder.TITLE, it.libraryOrder.first())
            }
        } finally { directory.deleteRecursively() }
    }

    private suspend fun withStore(action: suspend (androidx.datastore.core.DataStore<androidx.datastore.preferences.core.Preferences>) -> Unit) {
        val directory = Files.createTempDirectory("soundflow-appearance-").toFile()
        val job = SupervisorJob()
        val store = PreferenceDataStoreFactory.create(scope = CoroutineScope(job + Dispatchers.IO), produceFile = { File(directory, "isolated.preferences_pb") })
        try { withTimeout(5_000) { action(store) } } finally { job.cancelAndJoin(); directory.deleteRecursively() }
    }
}
