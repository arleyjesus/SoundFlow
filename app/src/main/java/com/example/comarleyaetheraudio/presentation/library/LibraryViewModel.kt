package com.example.comarleyaetheraudio.presentation.library

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.comarleyaetheraudio.data.local.FolderImportConflictException
import com.example.comarleyaetheraudio.data.local.entity.FolderEntity
import com.example.comarleyaetheraudio.data.player.AudioPlayerHandler
import com.example.comarleyaetheraudio.data.repository.AudioRepositoryImpl
import com.example.comarleyaetheraudio.data.repository.SettingsRepository
import com.example.comarleyaetheraudio.domain.model.Playlist
import com.example.comarleyaetheraudio.domain.model.Song
import com.example.comarleyaetheraudio.ui.theme.AppTheme
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext

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

    private val folderMutex = Mutex()
    private val _isUpdatingFolders = MutableStateFlow(false)
    val isUpdatingFolders: StateFlow<Boolean> = _isUpdatingFolders.asStateFlow()
    private val _folderMessage = MutableStateFlow<String?>(null)
    val folderMessage: StateFlow<String?> = _folderMessage.asStateFlow()

    fun clearFolderMessage() {
        _folderMessage.value = null
    }

    fun onAddFolder(uri: Uri) {
        runFolderOperation {
            val result = repository.importFolder(uri)
            "${result.added} canciones nuevas; ${result.alreadyPresent} ya estaban en la biblioteca."
        }
    }

    fun addSongsToPlaylist(playlistId: Long, songIds: List<Long>) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addSongsToPlaylist(playlistId, songIds)
        }
    }

    fun onRemoveFolder(folderPath: String) {
        runFolderOperation {
            repository.removeFolder(folderPath)
            "Carpeta retirada de la biblioteca. Los archivos siguen en tu dispositivo."
        }
    }

    private fun runFolderOperation(action: suspend () -> String) {
        viewModelScope.launch {
            if (!folderMutex.tryLock()) return@launch
            _isUpdatingFolders.value = true
            _folderMessage.value = null
            try {
                _folderMessage.value = withContext(Dispatchers.IO) { action() }
            } catch (e: CancellationException) {
                throw e
            } catch (e: FolderImportConflictException) {
                _folderMessage.value = "Esta carpeta comparte canciones con otra registrada. No se importó para conservar sus referencias."
            } catch (e: SecurityException) {
                _folderMessage.value = "No hay acceso a la carpeta. Vuelve a seleccionarla y concede permiso de lectura."
            } catch (e: Exception) {
                Log.e("SoundFlowFolders", "Fallo en la operacion de carpeta", e)
                _folderMessage.value = "No se pudo completar la operación. Se conservaron los datos anteriores; puedes intentarlo de nuevo."
            } finally {
                _isUpdatingFolders.value = false
                folderMutex.unlock()
            }
        }
    }
}
