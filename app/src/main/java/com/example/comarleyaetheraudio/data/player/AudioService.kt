package com.example.comarleyaetheraudio.data.player

import android.content.Intent
import androidx.media3.common.Player
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

/**
 * Servicio en primer plano (Foreground Service) que gestiona la reproducción continua
 * y la sesión multimedia en la cortina de notificaciones de Android.
 */
class AudioService : MediaSessionService() {

    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        // Inicializamos el motor del reproductor
        val playerHandler = AudioPlayerHandler(this)

        // Creamos la sesión nativa vinculada a ExoPlayer
        mediaSession = MediaSession.Builder(this, playerHandler.exoPlayer).build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
}