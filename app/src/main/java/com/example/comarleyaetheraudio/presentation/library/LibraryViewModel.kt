package com.example.comarleyaetheraudio.presentation.library

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.comarleyaetheraudio.data.local.PlaylistEntity
import com.example.comarleyaetheraudio.data.local.PlaylistSongCrossRef
import com.example.comarleyaetheraudio.data.local.dao.PlaylistDao
import com.example.comarleyaetheraudio.data.local.entity.FolderEntity
import com.example.comarleyaetheraudio.data.player.AudioPlayerHandler
import com.example.comarleyaetheraudio.data.repository.SettingsRepository
import com.example.comarleyaetheraudio.domain.model.AudioPlayerState
import com.example.comarleyaetheraudio.domain.model.Playlist
import com.example.comarleyaetheraudio.domain.model.Song
import com.example.comarleyaetheraudio.domain.repository.AudioRepository
import com.example.comarleyaetheraudio.ui.theme.AppTheme
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class LibraryViewModel(
    private val repository: AudioRepository,
    val playerHandler: AudioPlayerHandler,
    private val settingsRepository: SettingsRepository,
    private val playlistDao: PlaylistDao
) : ViewModel() {

    val isDarkMode: StateFlow<Boolean> = settingsRepository.isDarkMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    val currentTheme: StateFlow<AppTheme> = settingsRepository.selectedTheme
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppTheme.PRINCIPAL
        )

    fun onSelectTheme(theme: AppTheme) {
        viewModelScope.launch {
            settingsRepository.setAppThemeStyle(theme)
        }
    }

    val songs: StateFlow<List<Song>> = repository.getSongsFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val folders: StateFlow<List<FolderEntity>> = repository.getFoldersFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val playerState: StateFlow<AudioPlayerState> = playerHandler.playerState

    val playlists: StateFlow<List<Playlist>> = playlistDao.getAllPlaylists()
        .combine(songs) { entities, allSongs ->
            entities.map { entity ->
                val songIdsInPlaylist = playlistDao.getSongIdsForPlaylist(entity.id)
                val playlistSongs = allSongs.filter { song -> songIdsInPlaylist.contains(song.id) }
                Playlist(
                    id = entity.id,
                    name = entity.name,
                    songs = playlistSongs
                )
            }
        }
        .catch { emit(emptyList()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun createPlaylist(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            playlistDao.insertPlaylist(
                PlaylistEntity(name = name.trim())
            )
        }
    }

    fun addSongToPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            playlistDao.insertSongToPlaylist(
                PlaylistSongCrossRef(playlistId = playlistId, songId = songId)
            )
        }
    }

    fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            playlistDao.removeSongFromPlaylist(playlistId, songId)
        }
    }

    fun deletePlaylist(playlist: Playlist) {
        viewModelScope.launch {
            playlistDao.deletePlaylist(
                PlaylistEntity(id = playlist.id, name = playlist.name)
            )
        }
    }

    fun onAddFolder(folderUri: Uri) {
        viewModelScope.launch {
            repository.addAndScanFolder(folderUri)
        }
    }

    fun onRemoveFolder(folderUriString: String) {
        viewModelScope.launch {
            repository.removeFolder(folderUriString)
        }
    }

    fun onSongClick(song: Song) {
        playerHandler.playSong(song)
    }

    fun onTogglePlayPause() {
        playerHandler.togglePlayPause()
    }

    fun onToggleDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setDarkMode(enabled)
        }
    }

    val favoriteIds: StateFlow<List<Long>> = repository.getFavoriteSongIds()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    fun toggleFavorite(songId: Long) {
        viewModelScope.launch {
            val currentFavorites = favoriteIds.value
            val isFav = currentFavorites.contains(songId)
            repository.toggleFavorite(songId, isFav)
        }
    }

    fun updateSongTags(song: Song, newTitle: String, newArtist: String, newAlbum: String) {
        viewModelScope.launch {
            val success = com.example.comarleyaetheraudio.data.local.TagEditorUtil.editSongTags(
                song, newTitle, newArtist, newAlbum
            )
            if (success) {
                // Modificado exitosamente
            }
        }
    }
}