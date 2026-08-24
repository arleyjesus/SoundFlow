package com.example.comarleyaetheraudio.data.player

import android.content.ComponentName
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.comarleyaetheraudio.domain.model.AudioPlayerState
import com.example.comarleyaetheraudio.domain.model.Song
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.Virtualizer

class AudioPlayerHandler(private val context: Context) {



    private var player: Player? = null
    private val _playerState = MutableStateFlow(AudioPlayerState())
    val playerState: StateFlow<AudioPlayerState> = _playerState.asStateFlow()

    private var currentPlaylist: List<Song> = emptyList()
    private var progressJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    init {
        val sessionToken = SessionToken(context, ComponentName(context, SoundFlowMediaService::class.java))
        val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()

        controllerFuture.addListener({
            player = controllerFuture.get()

            // 1. MEJORA: Pausa inteligente y Audio Focus (Llamadas, WhatsApp, Desconexión de auriculares)
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .build()
            player?.setAudioAttributes(audioAttributes, true)

            setupPlayerListener()
        }, ContextCompat.getMainExecutor(context))
    }

    private fun setupPlayerListener() {
        player?.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _playerState.update { it.copy(isPlaying = isPlaying) }
                if (isPlaying) startProgressUpdate() else stopProgressUpdate()
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                super.onMediaItemTransition(mediaItem, reason)
                val songId = mediaItem?.mediaId?.toLongOrNull()
                val newSong = currentPlaylist.find { it.id == songId }
                if (newSong != null) {
                    // Extraer carátula nativamente del archivo local
                    val artBytes = extractArtworkBytes(newSong.contentUri)
                    _playerState.update {
                        it.copy(
                            currentSong = newSong,
                            duration = newSong.duration,
                            currentPosition = 0L,
                            artworkData = artBytes
                        )
                    }
                }
            }

            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                _playerState.update { it.copy(isShuffleEnabled = shuffleModeEnabled) }
            }
        })
    }

    /**
     * Extractor directo de carátulas nativo para etiquetas ID3 / FLAC / MP3
     */
    private fun extractArtworkBytes(contentUri: Uri): ByteArray? {
        val mmr = MediaMetadataRetriever()
        return try {
            mmr.setDataSource(context, contentUri)
            mmr.embeddedPicture
        } catch (_: Exception) {
            null
        } finally {
            try { mmr.release() } catch (_: Exception) {}
        }
    }

    fun playSong(song: Song, playlist: List<Song> = emptyList()) {
        if (playlist.isNotEmpty()) {
            currentPlaylist = playlist
        }

        val mediaItems = currentPlaylist.map { s ->
            MediaItem.Builder()
                .setMediaId(s.id.toString())
                .setUri(s.contentUri)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(s.title)
                        .setArtist(s.artist)
                        .setAlbumTitle(s.album)
                        .build()
                )
                .build()
        }

        val startIndex = currentPlaylist.indexOfFirst { it.id == song.id }.coerceAtLeast(0)
        player?.setMediaItems(mediaItems, startIndex, 0L)
        player?.prepare()
        player?.play()
    }

    fun togglePlayPause() {
        player?.let { if (it.isPlaying) it.pause() else it.play() }
    }

    fun seekTo(positionMs: Long) {
        player?.seekTo(positionMs)
        _playerState.update { it.copy(currentPosition = positionMs) }
    }

    fun seekForward(seconds: Int = 10) {
        player?.let {
            val newPos = (it.currentPosition + seconds * 1000).coerceAtMost(it.duration)
            seekTo(newPos)
        }
    }

    fun seekRewind(seconds: Int = 10) {
        player?.let {
            val newPos = (it.currentPosition - seconds * 1000).coerceAtLeast(0)
            seekTo(newPos)
        }
    }

    fun playNext() {
        if (player?.hasNextMediaItem() == true) {
            player?.seekToNextMediaItem()
        }
    }

    fun playPrevious() {
        if (player?.hasPreviousMediaItem() == true) {
            player?.seekToPreviousMediaItem()
        }
    }

    fun toggleShuffle() {
        player?.let { it.shuffleModeEnabled = !it.shuffleModeEnabled }
    }

    private fun startProgressUpdate() {
        stopProgressUpdate()
        progressJob = scope.launch {
            while (true) {
                player?.let { p ->
                    _playerState.update {
                        it.copy(
                            currentPosition = p.currentPosition,
                            duration = if (p.duration > 0) p.duration else it.duration
                        )
                    }
                }
                delay(500)
            }
        }
    }

    private fun stopProgressUpdate() {
        progressJob?.cancel()
    }

    var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null

    fun setupAudioEffects(audioSessionId: Int) {
        if (audioSessionId != android.media.audiofx.AudioEffect.ERROR_BAD_VALUE) {
            try {
                equalizer = Equalizer(0, audioSessionId).apply { enabled = true }
                bassBoost = BassBoost(0, audioSessionId).apply { enabled = true }
                virtualizer = Virtualizer(0, audioSessionId).apply { enabled = true }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Configuración de Transiciones Suaves y Reproducción Continuada
    fun enableSmoothTransitions() {
        player?.repeatMode = Player.REPEAT_MODE_OFF // Asegura la continuidad normal de la lista sin pausas forzadas
    }

    // Función para atenuación de volumen progresiva (Crossfade manual al pausar/cambiar)
    fun fadeOutAndPause(durationMs: Long = 1000) {
        val p = player ?: return
        val initialVolume = p.volume
        val steps = 10
        val stepDelay = durationMs / steps
        val volumeStep = initialVolume / steps

        Thread {
            for (i in 1..steps) {
                p.volume = (initialVolume - (i * volumeStep)).coerceAtLeast(0f)
                Thread.sleep(stepDelay)
            }
            p.pause()
            p.volume = initialVolume // Restablece el volumen original
        }.start()
    }
}