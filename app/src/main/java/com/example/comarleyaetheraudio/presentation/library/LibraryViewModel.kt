package com.example.comarleyaetheraudio.presentation.library

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.comarleyaetheraudio.data.local.FolderScanner
import com.example.comarleyaetheraudio.data.local.entity.FolderEntity
import com.example.comarleyaetheraudio.data.player.AudioPlayerHandler
import com.example.comarleyaetheraudio.data.repository.AudioRepositoryImpl
import com.example.comarleyaetheraudio.data.repository.SettingsRepository
import com.example.comarleyaetheraudio.domain.model.Playlist
import com.example.comarleyaetheraudio.domain.model.Song
import com.example.comarleyaetheraudio.ui.theme.AppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class LibraryViewModel(
    private val repository: AudioRepositoryImpl,
    val playerHandler: AudioPlayerHandler,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val songs: StateFlow<List<Song>> = repository.allSongs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val artistGrouped: StateFlow<Map<String, List<Song>>> = songs.map { list ->
        list.groupBy { it.artist }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val playlists: StateFlow<List<Playlist>> = repository.allPlaylists
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val folders: StateFlow<List<FolderEntity>> = repository.allFolders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteIds: StateFlow<List<Long>> = repository.favoriteIds
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val playerState = playerHandler.playerState

    val isDarkMode: StateFlow<Boolean> = settingsRepository.isDarkMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val currentTheme: StateFlow<AppTheme> = settingsRepository.selectedTheme
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppTheme.PRINCIPAL)

    fun onToggleDarkMode(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setDarkMode(enabled) }
    }

    fun onSelectTheme(theme: AppTheme) {
        viewModelScope.launch { settingsRepository.setSelectedTheme(theme) }
    }

    fun onTogglePlayPause() {
        playerHandler.togglePlayPause()
    }

    fun toggleFavorite(songId: Long) {
        viewModelScope.launch { repository.toggleFavorite(songId) }
    }

    fun createPlaylist(name: String) {
        viewModelScope.launch { repository.createPlaylist(name) }
    }

    fun renamePlaylist(playlistId: Long, newName: String) {
        viewModelScope.launch { repository.renamePlaylist(playlistId, newName) }
    }

    fun deletePlaylist(playlist: Playlist) {
        viewModelScope.launch { repository.deletePlaylist(playlist) }
    }

    fun addSongToPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch { repository.addSongToPlaylist(playlistId, songId) }
    }

    fun updateSongTags(song: Song, newTitle: String, newArtist: String, newAlbum: String) {
        viewModelScope.launch { repository.updateSongTags(song.id, newTitle, newArtist, newAlbum) }
    }

    // 1. AGREGA ESTA VARIABLE ARRIBA EN TU VIEWMODEL:
    private var isScanningFolder = false

    // 2. REEMPLAZA TU FUNCIÓN onAddFolder POR ESTA:
    fun onAddFolder(uri: Uri) {
        // Si ya está escaneando, ignoramos el toque extra para no sobrecargar el celular
        if (isScanningFolder) return

        viewModelScope.launch(Dispatchers.IO) {
            isScanningFolder = true // Bloqueamos nuevas peticiones

            try {
                val folderPath = uri.toString()
                val folderName = uri.lastPathSegment?.substringAfterLast(":") ?: "Carpeta"

                repository.insertFolder(FolderEntity(
                    path = folderPath,
                    name = folderName,
                    uriString = folderPath
                ))

                val scanner = FolderScanner(repository.context)
                val scannedSongs = scanner.scanFolderUri(uri, folderName)

                repository.insertSongsEntities(scannedSongs)
            } finally {
                // Siempre liberamos el bloqueo al terminar, aunque haya error
                isScanningFolder = false
            }
        }
    }

    fun onRemoveFolder(folderPath: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteSongsByFolderUri(folderPath)
            repository.deleteFolderByPath(folderPath)
        }
    }
}