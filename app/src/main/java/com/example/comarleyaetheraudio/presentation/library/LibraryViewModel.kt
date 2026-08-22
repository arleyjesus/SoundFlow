package com.example.comarleyaetheraudio.presentation.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.comarleyaetheraudio.data.player.AudioPlayerHandler
import com.example.comarleyaetheraudio.domain.model.AudioPlayerState
import com.example.comarleyaetheraudio.domain.model.Song
import com.example.comarleyaetheraudio.domain.usecase.GetLocalAudioFilesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LibraryViewModel(
    private val getLocalAudioFilesUseCase: GetLocalAudioFilesUseCase,
    val playerHandler: AudioPlayerHandler
) : ViewModel() {

    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs: StateFlow<List<Song>> = _songs.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val playerState: StateFlow<AudioPlayerState> = playerHandler.playerState

    fun loadSongs() {
        viewModelScope.launch {
            _isLoading.value = true
            val localSongs = getLocalAudioFilesUseCase()
            _songs.value = localSongs
            _isLoading.value = false
        }
    }

    fun onSongClick(song: Song) {
        playerHandler.playSong(song)
    }

    fun onTogglePlayPause() {
        playerHandler.togglePlayPause()
    }
}