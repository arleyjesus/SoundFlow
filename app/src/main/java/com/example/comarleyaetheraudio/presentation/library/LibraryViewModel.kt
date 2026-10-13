package com.example.comarleyaetheraudio.presentation.library

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.comarleyaetheraudio.data.local.FolderImportConflictException
import com.example.comarleyaetheraudio.data.local.FolderAccessMismatchException
import com.example.comarleyaetheraudio.domain.model.LibraryFolder
import com.example.comarleyaetheraudio.data.player.AudioPlayerHandler
import com.example.comarleyaetheraudio.domain.repository.AudioRepository
import com.example.comarleyaetheraudio.data.repository.SettingsRepository
import com.example.comarleyaetheraudio.domain.model.Playlist
import com.example.comarleyaetheraudio.domain.model.Song
import com.example.comarleyaetheraudio.domain.model.NetworkSettings
import com.example.comarleyaetheraudio.domain.model.NetworkFeature
import com.example.comarleyaetheraudio.ui.theme.AppTheme
import com.example.comarleyaetheraudio.ui.theme.AppearanceSettings
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext

class LibraryViewModel(
    private val repository: AudioRepository,
    val playerHandler: AudioPlayerHandler,
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    suspend fun inspectAudio(song: Song) = repository.inspectAudio(song.contentUri)
    val libraryOrder = settingsRepository.libraryOrder.stateIn(viewModelScope,
        SharingStarted.WhileSubscribed(5000), com.example.comarleyaetheraudio.domain.library.LibraryOrder.ADDED_NEWEST)
    fun onLibraryOrder(order: com.example.comarleyaetheraudio.domain.library.LibraryOrder) {
        viewModelScope.launch { settingsRepository.setLibraryOrder(order) }
    }

    val networkSettings: StateFlow<NetworkSettings?> = settingsRepository.networkSettings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun onNetworkFeature(feature: NetworkFeature, enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setNetworkFeature(feature, enabled) }
    }

    val songs: StateFlow<List<Song>> = repository.allSongs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val artistGrouped: StateFlow<Map<String, List<Song>>> = songs.map { list ->
        list.groupBy { it.artist }
    }.flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val playlists: StateFlow<List<Playlist>> = repository.observePlaylists(songs)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val folders: StateFlow<List<LibraryFolder>> = repository.allFolders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteIds: StateFlow<List<Long>> = repository.favoriteIds
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val playerState = playerHandler.playerState
    // Navigation needs identity/status, not the half-second progress or embedded image bytes.
    val navigationPlayerState = playerState.map { it.copy(currentPosition = 0, artworkData = null) }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000),
            com.example.comarleyaetheraudio.domain.model.AudioPlayerState())

    init {
        viewModelScope.launch {
            songs.map { list -> list.associateBy { it.id } }.flowOn(Dispatchers.Default)
                .collect { playerHandler.updateLibrarySongIndex(it) }
        }
    }

    override fun onCleared() {
        playerHandler.release()
        super.onCleared()
    }

    val isDarkMode: StateFlow<Boolean> = settingsRepository.isDarkMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val appearance: StateFlow<AppearanceSettings?> = settingsRepository.appearance
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun onToggleAnimations(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setAnimationsEnabled(enabled) }
    }

    fun onToggleTransparencies(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setTransparenciesEnabled(enabled) }
    }

    fun onFollowSystemTheme(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setFollowSystemTheme(enabled) }
    }

    fun onResetAppearance() {
        viewModelScope.launch { settingsRepository.resetAppearance() }
    }

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
    private val _folderAccessToRestore = MutableStateFlow<String?>(null)
    val folderAccessToRestore: StateFlow<String?> = _folderAccessToRestore.asStateFlow()

    fun clearFolderMessage() {
        _folderMessage.value = null
    }

    fun onAddFolder(uri: Uri) {
        runFolderOperation(uri.toString()) {
            val result = repository.importFolder(uri)
            "${result.added} canciones nuevas; ${result.alreadyPresent} ya estaban en la biblioteca."
        }
    }

    fun onRestoreFolderAccess(originalUri: String, selectedUri: Uri) {
        runFolderOperation(originalUri) {
            val result = repository.restoreFolderAccess(originalUri, selectedUri)
            "Acceso restaurado. ${result.added} canciones nuevas; ${result.alreadyPresent} ya estaban en la biblioteca."
        }
    }

    fun addSongsToPlaylist(playlistId: Long, songIds: List<Long>) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addSongsToPlaylist(playlistId, songIds)
        }
    }

    fun onRemoveFolder(folderPath: String) {
        runFolderOperation(folders.value.firstOrNull { it.path == folderPath }?.uriString ?: folderPath) {
            repository.removeFolder(folderPath)
            "Carpeta retirada de la biblioteca. Los archivos siguen en tu dispositivo."
        }
    }

    private fun runFolderOperation(folderUri: String, action: suspend () -> String) {
        viewModelScope.launch {
            if (!folderMutex.tryLock()) return@launch
            _isUpdatingFolders.value = true
            _folderMessage.value = null
            try {
                _folderMessage.value = withContext(Dispatchers.IO) { action() }
                if (_folderAccessToRestore.value == folderUri) _folderAccessToRestore.value = null
            } catch (e: CancellationException) {
                throw e
            } catch (e: FolderImportConflictException) {
                _folderMessage.value = "Esta carpeta comparte canciones con otra registrada. No se importó para conservar sus referencias."
            } catch (e: SecurityException) {
                _folderAccessToRestore.value = folderUri
                _folderMessage.value = "No hay acceso a la carpeta. Vuelve a seleccionarla y concede permiso de lectura."
            } catch (e: FolderAccessMismatchException) {
                _folderMessage.value = "Selecciona la misma carpeta para recuperar el acceso. No se modificó la biblioteca."
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
