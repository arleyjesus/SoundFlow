package com.example.comarleyaetheraudio.data.player

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.comarleyaetheraudio.domain.model.AudioPlayerState
import com.example.comarleyaetheraudio.domain.model.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AudioPlayerHandler(context: Context) {

    val exoPlayer = ExoPlayer.Builder(context).build()
    private val _playerState = MutableStateFlow(AudioPlayerState())
    val playerState: StateFlow<AudioPlayerState> = _playerState.asStateFlow()

    private var currentPlaylist: List<Song> = emptyList()
    private var progressJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    init {
        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _playerState.update { it.copy(isPlaying = isPlaying) }
                if (isPlaying) startProgressUpdate() else stopProgressUpdate()
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    playNext()
                }
            }
        })
    }

    fun playSong(song: Song, playlist: List<Song> = emptyList()) {
        if (playlist.isNotEmpty()) {
            currentPlaylist = playlist
        }
        _playerState.update {
            it.copy(
                currentSong = song,
                duration = song.duration,
                currentPosition = 0L
            )
        }
        val mediaItem = MediaItem.fromUri(song.contentUri)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.play()
    }

    fun togglePlayPause() {
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
        } else {
            exoPlayer.play()
        }
    }

    fun seekTo(positionMs: Long) {
        exoPlayer.seekTo(positionMs)
        _playerState.update { it.copy(currentPosition = positionMs) }
    }

    fun seekForward(seconds: Int = 10) {
        val newPos = (exoPlayer.currentPosition + seconds * 1000).coerceAtMost(exoPlayer.duration)
        seekTo(newPos)
    }

    fun seekRewind(seconds: Int = 10) {
        val newPos = (exoPlayer.currentPosition - seconds * 1000).coerceAtLeast(0)
        seekTo(newPos)
    }

    fun playNext() {
        if (currentPlaylist.isEmpty()) return
        val currentIndex = currentPlaylist.indexOfFirst { it.id == _playerState.value.currentSong?.id }
        if (currentIndex != -1 && currentIndex < currentPlaylist.size - 1) {
            playSong(currentPlaylist[currentIndex + 1], currentPlaylist)
        }
    }

    fun playPrevious() {
        if (currentPlaylist.isEmpty()) return
        val currentIndex = currentPlaylist.indexOfFirst { it.id == _playerState.value.currentSong?.id }
        if (currentIndex > 0) {
            playSong(currentPlaylist[currentIndex - 1], currentPlaylist)
        }
    }

    private fun startProgressUpdate() {
        stopProgressUpdate()
        progressJob = scope.launch {
            while (true) {
                _playerState.update {
                    it.copy(
                        currentPosition = exoPlayer.currentPosition,
                        duration = if (exoPlayer.duration > 0) exoPlayer.duration else it.duration
                    )
                }
                delay(500)
            }
        }
    }

    private fun stopProgressUpdate() {
        progressJob?.cancel()
    }
}