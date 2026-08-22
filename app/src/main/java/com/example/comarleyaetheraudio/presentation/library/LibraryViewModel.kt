package com.example.comarleyaetheraudio.presentation.library

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.comarleyaetheraudio.data.local.entity.FolderEntity
import com.example.comarleyaetheraudio.data.player.AudioPlayerHandler
import com.example.comarleyaetheraudio.domain.model.AudioPlayerState
import com.example.comarleyaetheraudio.domain.model.Song
import com.example.comarleyaetheraudio.domain.repository.AudioRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LibraryViewModel(
    private val repository: AudioRepository,
    val playerHandler: AudioPlayerHandler
) : ViewModel() {

    // Lista reactiva de canciones guardadas en Room
    val songs: StateFlow<List<Song>> = repository.getSongsFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Lista reactiva de carpetas guardadas en Room
    val folders: StateFlow<List<FolderEntity>> = repository.getFoldersFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val playerState: StateFlow<AudioPlayerState> = playerHandler.playerState

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
}