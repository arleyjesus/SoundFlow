package com.example.comarleyaetheraudio.data.player

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Process
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import androidx.media3.session.SessionError
import com.example.comarleyaetheraudio.domain.model.AudioEffectsState
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.SettableFuture
import java.util.concurrent.Executors
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import com.example.comarleyaetheraudio.domain.library.ListeningTracker
import com.example.comarleyaetheraudio.data.repository.SettingsRepository

/**
 * Servicio en segundo plano para mantener la reproducción activa y vincular
 * el reproductor con el panel de notificaciones nativo de Android.
 */
@OptIn(UnstableApi::class)
class SoundFlowMediaService : MediaSessionService() {

    private var mediaSession: MediaSession? = null
    private val effects = AudioEffectsEngine(AndroidAudioEffectsFactory(::refreshEffectsFromDevice))
    private val effectsWorker = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())
    @Volatile private var destroyed = false
    private var effectsRevision = 0L
    private val historyScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val listeningTracker = ListeningTracker()

    private fun refreshEffectsFromDevice() {
        if (destroyed) return
        runCatching {
            effectsWorker.execute {
                effects.reconcile()
                val state = effects.state
                mainHandler.post { if (!destroyed) publishEffects(state) }
            }
        }
    }

    private fun publishEffects(state: AudioEffectsState): Bundle {
        val bundle = AudioEffectsProtocol.encode(state.copy(revision = ++effectsRevision))
        mediaSession?.let { session ->
            session.connectedControllers.filter { it.uid == Process.myUid() }.forEach {
                session.sendCustomCommand(it, AudioEffectsProtocol.command, bundle)
            }
        }
        return bundle
    }

    private val sessionCallback = object : MediaSession.Callback {
        override fun onConnect(session: MediaSession, controller: MediaSession.ControllerInfo): MediaSession.ConnectionResult {
            val result = super.onConnect(session, controller)
            if (controller.uid != Process.myUid()) return result
            return MediaSession.ConnectionResult.AcceptedResultBuilder(session)
                .setAvailableSessionCommands(result.availableSessionCommands.buildUpon()
                    .add(AudioEffectsProtocol.command).build())
                .setAvailablePlayerCommands(result.availablePlayerCommands).build()
        }

        override fun onCustomCommand(session: MediaSession, controller: MediaSession.ControllerInfo,
            customCommand: SessionCommand, args: Bundle): ListenableFuture<SessionResult> {
            if (customCommand.customAction != AudioEffectsProtocol.ACTION) {
                return super.onCustomCommand(session, controller, customCommand, args)
            }
            if (controller.uid != Process.myUid()) {
                return Futures.immediateFuture(SessionResult(SessionError.ERROR_PERMISSION_DENIED))
            }
            if (destroyed) return Futures.immediateFuture(SessionResult(SessionError.ERROR_SESSION_DISCONNECTED))
            val sessionId = (session.player as ExoPlayer).audioSessionId
            val future = SettableFuture.create<SessionResult>()
            effectsWorker.execute {
                val code = try {
                    when (args.getString(AudioEffectsProtocol.OP)) {
                        AudioEffectsProtocol.GET -> Unit
                        AudioEffectsProtocol.ENABLE -> {
                            require(args.containsKey(AudioEffectsProtocol.VALUE))
                            effects.setEnabled(args.getBoolean(AudioEffectsProtocol.VALUE))
                        }
                        AudioEffectsProtocol.BAND -> {
                            require(args.containsKey(AudioEffectsProtocol.INDEX) && args.containsKey(AudioEffectsProtocol.VALUE))
                            require(args.getInt(AudioEffectsProtocol.INDEX) in effects.state.bands.indices)
                            effects.setBandLevel(args.getInt(AudioEffectsProtocol.INDEX), args.getInt(AudioEffectsProtocol.VALUE))
                        }
                        AudioEffectsProtocol.BASS -> {
                            require(args.containsKey(AudioEffectsProtocol.VALUE))
                            effects.setBassStrength(args.getInt(AudioEffectsProtocol.VALUE))
                        }
                        AudioEffectsProtocol.VIRTUALIZER -> {
                            require(args.containsKey(AudioEffectsProtocol.VALUE))
                            effects.setVirtualizerStrength(args.getInt(AudioEffectsProtocol.VALUE))
                        }
                        AudioEffectsProtocol.RESET -> effects.reset()
                        AudioEffectsProtocol.RETRY -> effects.attach(sessionId, force = true)
                        else -> throw IllegalArgumentException("Unknown audio effect operation")
                    }
                    SessionResult.RESULT_SUCCESS
                } catch (_: Exception) { SessionError.ERROR_BAD_VALUE }
                val state = effects.state
                mainHandler.post {
                    if (destroyed) future.set(SessionResult(SessionError.ERROR_SESSION_DISCONNECTED))
                    else future.set(SessionResult(code, publishEffects(state)))
                }
            }
            return future
        }
    }

    override fun onCreate() {
        super.onCreate()
        val player = ExoPlayer.Builder(this)
            .setAudioAttributes(
                AudioAttributes.Builder().setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC).build(),
                true
            )
            .setHandleAudioBecomingNoisy(true)
            .build()
        player.addListener(object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: androidx.media3.common.MediaItem?, reason: Int) {
                listeningTracker.reset()
            }
            override fun onAudioSessionIdChanged(audioSessionId: Int) {
                if (destroyed) return
                effectsWorker.execute {
                    effects.attach(audioSessionId)
                    val state = effects.state
                    mainHandler.post { if (!destroyed) publishEffects(state) }
                }
            }
        })
        mediaSession = MediaSession.Builder(this, player).setCallback(sessionCallback).build()
        historyScope.launch {
            try {
                val settings = SettingsRepository(applicationContext)
                settings.listeningEnabled.collectLatest { enabled ->
                    listeningTracker.reset()
                    if (enabled) while (isActive) {
                        delay(1000)
                        val credited = listeningTracker.sample(player.currentMediaItem?.mediaId?.toLongOrNull(),
                            player.currentPosition, player.duration, player.isPlaying, android.os.SystemClock.elapsedRealtime())
                        if (credited != null) withContext(Dispatchers.IO) { settings.recordListening(credited) }
                    }
                }
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) { android.util.Log.w("SoundFlow", "Listening history unavailable", e) }
        }
        val initialSessionId = player.audioSessionId
        effectsWorker.execute {
            effects.attach(initialSessionId)
            val state = effects.state
            mainHandler.post { if (!destroyed) publishEffects(state) }
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        destroyed = true
        historyScope.cancel()
        effectsWorker.execute { effects.release() }
        effectsWorker.shutdown()
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
}
