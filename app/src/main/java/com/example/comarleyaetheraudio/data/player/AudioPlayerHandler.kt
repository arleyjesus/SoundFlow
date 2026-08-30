package com.example.comarleyaetheraudio.data.player

import android.content.ComponentName
import android.content.Context
import android.media.MediaMetadataRetriever
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.Virtualizer
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AudioPlayerHandler(private val context: Context) {

    private var player: Player? = null
    private val _playerState = MutableStateFlow(AudioPlayerState())
    val playerState: StateFlow<AudioPlayerState> = _playerState.asStateFlow()

    private var currentPlaylist: List<Song> = emptyList()
    private var progressJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null

    init {
        val sessionToken = SessionToken(context, ComponentName(context, SoundFlowMediaService::class.java))
        val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()

        controllerFuture.addListener({
            player = controllerFuture.get()

            // 🎧 CONFIGURACIÓN COMPLETA PARA COMPATIBILIDAD CON AUDÍFONOS BLUETOOTH / INALÁMBRICOS
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .setAllowedCapturePolicy(C.ALLOW_CAPTURE_BY_ALL)
                .build()

            // Activa el Audio Focus y el enrutamiento dinámico a Bluetooth/Auriculares
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

            // Manejo de errores de salida o desconexión de audio Bluetooth
            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                super.onPlayerError(error)
                // Reintenta preparar la reproducción si se perdió la salida Bluetooth
                player?.prepare()
            }
        })
    }

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

    // ==========================================
    // SECCIÓN DE EFECTOS AUDIO Y ECUALIZADOR
    // ==========================================
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

    fun setEqualizerBandLevel(bandIndex: Short, valueInDb: Int) {
        equalizer?.let { eq ->
            try {
                if (!eq.enabled) eq.enabled = true
                val milliBels = (valueInDb * 100).toShort()
                val range = eq.bandLevelRange
                if (range != null && range.size >= 2) {
                    val clamped = milliBels.coerceIn(range[0], range[1])
                    eq.setBandLevel(bandIndex, clamped)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun setBassBoostLevel(strength: Int) {
        bassBoost?.let { bb ->
            try {
                if (!bb.enabled) bb.enabled = true
                bb.setStrength(strength.toShort().coerceIn(0, 1000))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun enableSmoothTransitions() {
        player?.repeatMode = Player.REPEAT_MODE_OFF
    }

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
            p.volume = initialVolume
        }.start()
    }

    // ==========================================
    // LÓGICA DEL TEMPORIZADOR DE APAGADO
    // ==========================================
    private var sleepTimerJob: Job? = null
    private val _sleepTimerMinutes = MutableStateFlow(0)
    val sleepTimerMinutes: StateFlow<Int> = _sleepTimerMinutes.asStateFlow()

    fun startSleepTimer(minutes: Int, stopAfterCurrentSong: Boolean = false) {
        cancelSleepTimer()
        _sleepTimerMinutes.value = minutes

        if (stopAfterCurrentSong) {
            player?.addListener(object : Player.Listener {
                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    if (reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO) {
                        fadeOutAndPause(2000)
                        cancelSleepTimer()
                        player?.removeListener(this)
                    }
                }
            })
            return
        }

        sleepTimerJob = scope.launch {
            var remainingSeconds = minutes * 60
            while (remainingSeconds > 0) {
                delay(1000)
                remainingSeconds--
                _sleepTimerMinutes.value = (remainingSeconds / 60) + 1
            }
            fadeOutAndPause(2000)
            cancelSleepTimer()
        }
    }

    fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        sleepTimerJob = null
        _sleepTimerMinutes.value = 0
    }
}