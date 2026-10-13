package com.example.comarleyaetheraudio.data.player

import android.content.ComponentName
import android.content.Context
import com.example.comarleyaetheraudio.domain.playback.SleepTimerState
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.net.Uri
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.PlaybackException
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.example.comarleyaetheraudio.domain.model.AudioEffectsState
import com.example.comarleyaetheraudio.domain.model.AudioPlayerState
import com.example.comarleyaetheraudio.domain.model.Song
import com.example.comarleyaetheraudio.domain.playback.playbackDuration
import com.example.comarleyaetheraudio.domain.playback.boundedPlaybackPosition
import com.example.comarleyaetheraudio.domain.playback.shiftedPlaybackPosition
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.Futures
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class AudioPlayerHandler(private val context: Context) {

    private var player: Player? = null
    private val _playerState = MutableStateFlow(AudioPlayerState())
    val playerState: StateFlow<AudioPlayerState> = _playerState.asStateFlow()

    private var currentPlaylist: List<Song> = emptyList()
    private var progressJob: Job? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var released = false
    private var pendingPlay: Triple<Song, List<Song>, Boolean>? = null
    private var librarySongsById: Map<Long, Song> = emptyMap()
    private var artworkJob: Job? = null
    private var artworkUri: Uri? = null
    private var transitionJob: Job? = null
    private var transitionVolume: Float? = null
    private var transitionGeneration = 0L
    private var timerListener: Player.Listener? = null

    private val _audioEffectsState = MutableStateFlow(AudioEffectsState())
    val audioEffectsState: StateFlow<AudioEffectsState> = _audioEffectsState.asStateFlow()
    private var effectsCommandJob: Job? = null
    private var effectsRequestGeneration = 0L

    // Legacy configuration, retained for compatibility; not an implemented two-track crossfade.
    private var crossfadeDurationMs: Long = 2000L

    private fun connect() {
        if (released) return
        effectsRequestGeneration++
        effectsCommandJob?.cancel()
        _audioEffectsState.value = AudioEffectsState()
        controllerFuture?.let { MediaController.releaseFuture(it) }
        _playerState.update { it.copy(isConnecting = true, errorMessage = null) }
        val sessionToken = SessionToken(context, ComponentName(context, SoundFlowMediaService::class.java))
        val future = MediaController.Builder(context, sessionToken)
            .setListener(object : MediaController.Listener {
                override fun onCustomCommand(controller: MediaController, command: SessionCommand,
                    args: Bundle): ListenableFuture<SessionResult> {
                    if (command.customAction != AudioEffectsProtocol.ACTION) {
                        return super.onCustomCommand(controller, command, args)
                    }
                    if (!released && player === controller) {
                        runCatching { applyEffectsState(AudioEffectsProtocol.decode(args)) }
                    }
                    return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
                }

                override fun onDisconnected(controller: MediaController) {
                    if (released || player !== controller) return
                    stopProgressUpdate()
                    artworkJob?.cancel()
                    artworkUri = null
                    cancelTransition()
                    cancelSleepTimer()
                    effectsRequestGeneration++
                    effectsCommandJob?.cancel()
                    _audioEffectsState.value = AudioEffectsState(isConnecting = false,
                        errorMessage = "Sin conexión con el reproductor. Vuelve a conectar desde el reproductor.")
                    player = null
                    _playerState.update { it.copy(isPlaying = false, isConnecting = false,
                        isBuffering = false, errorMessage = "Se perdió la conexión con el reproductor. Vuelve a conectar.") }
                }
            }).buildAsync()
        controllerFuture = future
        future.addListener({
            if (released || controllerFuture !== future) return@addListener
            try {
                val controller = future.get()
                player = controller
                controller.addListener(listener)
                _playerState.update { it.copy(isConnecting = false) }
                requestAudioEffects(AudioEffectsProtocol.GET)
                val request = pendingPlay
                pendingPlay = null
                if (request != null) playSong(request.first, request.second, request.third) else refreshPlayerState()
                if (controller.isPlaying) startProgressUpdate()
            } catch (e: Exception) {
                Log.e("SoundFlowPlayer", "No se pudo conectar MediaController", e)
                player = null
                _audioEffectsState.value = AudioEffectsState(isConnecting = false,
                    errorMessage = "No se pudo conectar el panel de audio. Reintenta desde el reproductor.")
                _playerState.update { it.copy(isPlaying = false, isConnecting = false,
                    isBuffering = false, errorMessage = "No se pudo conectar el reproductor. Puedes reintentar.") }
            }
        }, ContextCompat.getMainExecutor(context))
    }

    private val listener = object : Player.Listener {
            override fun onEvents(player: Player, events: Player.Events) {
                refreshPlayerState()
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _playerState.update { it.copy(isPlaying = isPlaying) }
                if (isPlaying) startProgressUpdate() else stopProgressUpdate()
            }

            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                _playerState.update { it.copy(isShuffleEnabled = shuffleModeEnabled) }
            }

            override fun onPlayerError(error: PlaybackException) {
                Log.e("SoundFlowPlayer", "Error de reproduccion ${error.errorCode}", error)
                _playerState.update { it.copy(isPlaying = false, isBuffering = false,
                    errorMessage = playbackMessage(error)) }
            }
        }

    private fun playbackMessage(error: PlaybackException): String = when (error.errorCode) {
        PlaybackException.ERROR_CODE_IO_NO_PERMISSION -> "No hay permiso para leer esta canción. Restaura el acceso en Carpetas y reintenta."
        PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND -> "No se encuentra el archivo. Comprueba que el almacenamiento está disponible."
        else -> "No se pudo reproducir esta canción. Puedes reintentar o elegir otra."
    }

    fun updateLibrarySongs(songs: List<Song>) {
        updateLibrarySongIndex(songs.associateBy { it.id })
    }

    fun updateLibrarySongIndex(songsById: Map<Long, Song>) {
        librarySongsById = songsById
        refreshPlayerState()
    }

    private fun refreshPlayerState() {
        val p = player ?: return
        val songId = p.currentMediaItem?.mediaId?.toLongOrNull()
        val song = songId?.let(librarySongsById::get) ?: currentPlaylist.find { it.id == songId }
        val duration = playbackDuration(p.duration, song?.duration ?: 0)
        _playerState.update { it.copy(currentSong = song, isPlaying = p.isPlaying,
            duration = duration, currentPosition = boundedPlaybackPosition(p.currentPosition, duration),
            isShuffleEnabled = p.shuffleModeEnabled, isBuffering = p.playbackState == Player.STATE_BUFFERING,
            errorMessage = p.playerError?.let(::playbackMessage)) }
        if (artworkUri != song?.contentUri) {
            artworkJob?.cancel()
            artworkUri = song?.contentUri
            _playerState.update { it.copy(artworkData = null) }
            if (song != null) artworkJob = scope.launch {
                val bytes = withContext(Dispatchers.IO) { extractArtworkBytes(song.contentUri) }
                if (_playerState.value.currentSong?.contentUri == song.contentUri &&
                    _playerState.value.currentSong?.id == song.id) {
                    _playerState.update { it.copy(artworkData = bytes) }
                }
            }
        }
    }

    fun retryPlayback() {
        if (released || _playerState.value.isConnecting) return
        cancelTransition()
        val p = player
        if (p == null) {
            connect()
        } else {
            _playerState.update { it.copy(errorMessage = null) }
            p.prepare()
            p.play()
        }
    }

    fun release() {
        if (released) return
        released = true
        pendingPlay = null
        cancelTransition()
        cancelSleepTimer()
        player?.removeListener(listener)
        controllerFuture?.let { MediaController.releaseFuture(it) }
        controllerFuture = null
        player = null
        _playerState.update { it.copy(isConnecting = false, isBuffering = false, isPlaying = false) }
        scope.cancel()
        _audioEffectsState.value = AudioEffectsState(isConnecting = false)
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

    fun playSong(song: Song, playlist: List<Song> = emptyList(), shuffle: Boolean = false) {
        if (released) return
        if (player == null) {
            artworkJob?.cancel()
            artworkUri = null
            pendingPlay = Triple(song, playlist.toList(), shuffle)
            _playerState.update { it.copy(currentSong = song, duration = song.duration,
                currentPosition = 0, artworkData = null) }
            if (!_playerState.value.isConnecting) connect()
            return
        }
        cancelTransition()
        _playerState.update { it.copy(errorMessage = null) }
        // Asignación de la lista exacta como contexto único de reproducción
        val request = com.example.comarleyaetheraudio.domain.playback.playbackRequest(song.id, playlist.map { it.id }, shuffle)
        val byId = playlist.associateBy { it.id } + (song.id to song)
        currentPlaylist = request.ids.map { byId.getValue(it) }

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

        val startIndex = request.startIndex

        player?.let { p ->
            p.stop()
            p.clearMediaItems() // Limpia colas de reproducción anteriores
            p.setMediaItems(mediaItems, startIndex, 0L)
            p.shuffleModeEnabled = request.shuffle

            // ⚡ Mantiene la reproducción cíclica infinita dentro de la playlist
            p.repeatMode = Player.REPEAT_MODE_ALL

            p.prepare()
            p.volume = 1.0f
            p.play()
        }
    }

    fun togglePlayPause() {
        cancelTransition()
        if (_playerState.value.errorMessage != null) {
            retryPlayback()
            return
        }
        player?.let { if (it.isPlaying) it.pause() else it.play() }
    }

    fun seekTo(positionMs: Long) {
        val target = boundedPlaybackPosition(positionMs, _playerState.value.duration)
        player?.seekTo(target)
        _playerState.update { it.copy(currentPosition = target) }
    }

    fun seekForward(seconds: Int = 10) {
        player?.let {
            val newPos = shiftedPlaybackPosition(it.currentPosition, seconds.toLong() * 1000, _playerState.value.duration)
            seekTo(newPos)
        }
    }

    fun seekRewind(seconds: Int = 10) {
        player?.let {
            val newPos = shiftedPlaybackPosition(it.currentPosition, -seconds.toLong() * 1000, _playerState.value.duration)
            seekTo(newPos)
        }
    }

    fun toggleShuffle() {
        player?.let { it.shuffleModeEnabled = !it.shuffleModeEnabled }
    }

    fun playNext() {
        if (player?.hasNextMediaItem() == true) {
            applyCrossfadeAndSwitch { player?.seekToNextMediaItem() }
        }
    }

    fun playPrevious() {
        if (player?.hasPreviousMediaItem() == true) {
            applyCrossfadeAndSwitch { player?.seekToPreviousMediaItem() }
        }
    }

    /**
     * Atenúa el volumen durante un cambio manual (300 ms).
     * No mezcla audio de dos pistas ni implementa crossfade automático.
     */
    private fun applyCrossfadeAndSwitch(action: () -> Unit) {
        cancelTransition()
        val p = player ?: run { action(); return }
        if (!p.isPlaying) {
            action()
            return
        }

        val generation = transitionGeneration
        val initialVolume = p.volume
        transitionVolume = initialVolume
        transitionJob = scope.launch {
            val manualSkipFadeMs = 300L
            val steps = 5
            val stepDelay = manualSkipFadeMs / steps
            try {
                for (i in 1..steps) {
                    p.volume = initialVolume * (1f - i.toFloat() / steps)
                    delay(stepDelay)
                }
                action()
            } finally {
                if (generation == transitionGeneration) {
                    p.volume = initialVolume
                    transitionVolume = null
                }
            }
        }
    }

    private fun cancelTransition() {
        transitionGeneration++
        transitionJob?.cancel()
        transitionJob = null
        transitionVolume?.let { player?.volume = it }
        transitionVolume = null
    }

    private fun startProgressUpdate() {
        stopProgressUpdate()
        progressJob = scope.launch {
            while (true) {
                player?.let { p ->
                    _playerState.update {
                        it.copy(
                            currentPosition = boundedPlaybackPosition(p.currentPosition, playbackDuration(p.duration, it.duration)),
                            duration = playbackDuration(p.duration, it.duration)
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

    fun setAudioEffectsEnabled(enabled: Boolean) = requestAudioEffects(AudioEffectsProtocol.ENABLE) {
        putBoolean(AudioEffectsProtocol.VALUE, enabled)
    }

    fun setEqualizerBandLevelMb(band: Int, levelMb: Int) = requestAudioEffects(AudioEffectsProtocol.BAND) {
        putInt(AudioEffectsProtocol.INDEX, band); putInt(AudioEffectsProtocol.VALUE, levelMb)
    }

    fun setBassBoostStrength(strength: Int) = requestAudioEffects(AudioEffectsProtocol.BASS) {
        putInt(AudioEffectsProtocol.VALUE, strength)
    }

    fun setVirtualizerStrength(strength: Int) = requestAudioEffects(AudioEffectsProtocol.VIRTUALIZER) {
        putInt(AudioEffectsProtocol.VALUE, strength)
    }

    fun resetAudioEffects() = requestAudioEffects(AudioEffectsProtocol.RESET)
    fun refreshAudioEffects() {
        if (released) return
        if (player == null) {
            if (!_playerState.value.isConnecting) connect()
        } else requestAudioEffects(AudioEffectsProtocol.RETRY)
    }

    private fun applyEffectsState(state: AudioEffectsState) {
        if (state.revision >= _audioEffectsState.value.revision) {
            _audioEffectsState.value = state.copy(isUpdating = effectsCommandJob?.isActive == true)
        }
    }

    private fun requestAudioEffects(operation: String, arguments: Bundle.() -> Unit = {}) {
        val controller = player as? MediaController ?: return
        if (released) return
        val generation = ++effectsRequestGeneration
        effectsCommandJob?.cancel()
        _audioEffectsState.update { it.copy(isUpdating = true) }
        effectsCommandJob = scope.launch {
            try {
                check(controller.availableSessionCommands.contains(AudioEffectsProtocol.command))
                val args = Bundle().apply { putString(AudioEffectsProtocol.OP, operation); arguments() }
                val result = withTimeout(5000) {
                    val future = controller.sendCustomCommand(AudioEffectsProtocol.command, args)
                    suspendCancellableCoroutine<SessionResult> { continuation ->
                        future.addListener({
                            try { continuation.resume(future.get()) }
                            catch (e: Exception) { continuation.resumeWithException(e) }
                        }, ContextCompat.getMainExecutor(context))
                        continuation.invokeOnCancellation { future.cancel(false) }
                    }
                }
                if (!released && player === controller && generation == effectsRequestGeneration) {
                    applyEffectsState(AudioEffectsProtocol.decode(result.extras))
                    check(result.resultCode == SessionResult.RESULT_SUCCESS)
                }
            } catch (e: CancellationException) {
                if (e is kotlinx.coroutines.TimeoutCancellationException && player === controller && !released &&
                    generation == effectsRequestGeneration) {
                    _audioEffectsState.update { it.copy(isConnecting = false,
                        errorMessage = "El panel de audio tardó demasiado en responder. Vuelve a comprobarlo.") }
                }
                throw e
            } catch (_: Exception) {
                if (!released && player === controller && generation == effectsRequestGeneration) _audioEffectsState.update {
                    it.copy(isConnecting = false, errorMessage = "No se pudo confirmar el ajuste de audio. Vuelve a comprobarlo.")
                }
            } finally {
                if (!released && player === controller && generation == effectsRequestGeneration) {
                    _audioEffectsState.update { it.copy(isUpdating = false) }
                }
            }
        }
    }

    fun setCrossfadeDuration(durationMs: Long) {
        this.crossfadeDurationMs = durationMs
    }

    fun fadeOutAndPause(durationMs: Long = 1000) {
        cancelTransition()
        val p = player ?: return
        val initialVolume = p.volume
        val steps = 10
        val stepDelay = durationMs.coerceAtLeast(0) / steps
        val volumeStep = initialVolume / steps

        val generation = transitionGeneration
        transitionVolume = initialVolume
        transitionJob = scope.launch {
            try {
                for (i in 1..steps) {
                    p.volume = (initialVolume - (i * volumeStep)).coerceAtLeast(0f)
                    delay(stepDelay)
                }
                p.pause()
            } finally {
                if (generation == transitionGeneration) {
                    p.volume = initialVolume
                    transitionVolume = null
                }
            }
        }
    }

    // =========================================================================
    // TEMPORIZADOR DE APAGADO (Sleep Timer)
    // =========================================================================
    private var sleepTimerJob: Job? = null
    private var timerFadeJob: Job? = null
    private val _sleepTimerMinutes = MutableStateFlow(0)
    val sleepTimerMinutes: StateFlow<Int> = _sleepTimerMinutes.asStateFlow()
    private val _sleepTimerState = MutableStateFlow<SleepTimerState>(SleepTimerState.Off)
    val sleepTimerState: StateFlow<SleepTimerState> = _sleepTimerState.asStateFlow()

    private fun updateTimerState(state: SleepTimerState) {
        _sleepTimerState.value = state
        // Retained for callers using the original minutes-only contract.
        _sleepTimerMinutes.value = state.remainingMinutes
    }

    fun startSleepTimer(minutes: Int, stopAfterCurrentSong: Boolean = false) {
        cancelSleepTimer()
        if (minutes <= 0) return
        updateTimerState(SleepTimerState.CountingDown(minutes.coerceAtMost(1440)))

        fun waitForCurrentSong() {
            val endListener = object : Player.Listener {
                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    if (reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO ||
                        reason == Player.MEDIA_ITEM_TRANSITION_REASON_REPEAT) {
                        player?.pause()
                        cancelSleepTimer()
                    }
                }
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_ENDED) cancelSleepTimer()
                }
            }
            timerListener = endListener
            player?.addListener(endListener)
            updateTimerState(SleepTimerState.WaitingForTrackEnd)
        }

        sleepTimerJob = scope.launch {
            var remainingSeconds = minutes.coerceIn(1, 1440) * 60
            while (remainingSeconds > 0) {
                delay(1000)
                remainingSeconds--
                updateTimerState(SleepTimerState.countdown(remainingSeconds))
            }
            if (stopAfterCurrentSong && player?.isPlaying == true) {
                waitForCurrentSong()
            } else {
                fadeOutAndPause(2000)
                timerFadeJob = transitionJob
                timerFadeJob?.join()
                timerFadeJob = null
                cancelSleepTimer()
            }
        }
    }

    fun cancelSleepTimer() {
        if (timerFadeJob != null && timerFadeJob === transitionJob) cancelTransition()
        timerFadeJob = null
        timerListener?.let { player?.removeListener(it) }
        timerListener = null
        sleepTimerJob?.cancel()
        sleepTimerJob = null
        updateTimerState(SleepTimerState.Off)
    }

    init {
        connect()
    }
}
