package com.example.comarleyaetheraudio.presentation

import android.net.Uri
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.comarleyaetheraudio.domain.model.AudioPlayerState
import com.example.comarleyaetheraudio.domain.model.Song
import com.example.comarleyaetheraudio.presentation.player.MiniPlayerCard
import com.example.comarleyaetheraudio.presentation.player.PlaybackStatusBanner
import com.example.comarleyaetheraudio.presentation.player.FullPlayerSheet
import com.example.comarleyaetheraudio.presentation.components.dialogs.SleepTimerDialog
import com.example.comarleyaetheraudio.domain.playback.SleepTimerState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PlaybackControlsTest {
    @get:Rule val compose = createComposeRule()
    private val song = Song(1, "Pista de prueba", "Artista", "Album", duration = 60_000,
        contentUri = Uri.parse("content://soundflow.test/audio/1"), albumArtUri = null,
        size = 1, mimeType = "audio/mpeg")

    @Test fun connectingDisablesPlaybackAndNext() {
        compose.setContent {
            MaterialTheme { MiniPlayerCard(AudioPlayerState(currentSong = song), {}, {}, {}, {}, {}) }
        }
        compose.onNodeWithText("Conectando reproductor…").assertExists()
        compose.onNodeWithContentDescription("Reproducir").assertIsNotEnabled()
        compose.onNodeWithContentDescription("Siguiente").assertIsNotEnabled()
    }

    @Test fun pauseAndNextDoNotAlsoOpenThePlayer() {
        var expanded = 0
        var paused = 0
        var next = 0
        compose.setContent {
            MaterialTheme { MiniPlayerCard(AudioPlayerState(currentSong = song, isPlaying = true, isConnecting = false),
                { expanded++ }, { paused++ }, { next++ }, {}, {}) }
        }
        compose.onNodeWithContentDescription("Pausar").performClick()
        compose.onNodeWithContentDescription("Siguiente").performClick()
        compose.runOnIdle { assertEquals(1, paused); assertEquals(1, next); assertEquals(0, expanded) }
        compose.onNodeWithText(song.title).performClick()
        compose.runOnIdle { assertEquals(1, expanded) }
    }

    @Test fun errorOffersExplicitRetryWithoutStartingPlaybackOnRender() {
        var retries = 0
        compose.setContent {
            MaterialTheme { PlaybackStatusBanner(AudioPlayerState(isConnecting = false,
                errorMessage = "Restaura el permiso")) { retries++ } }
        }
        compose.onNodeWithText("Restaura el permiso").assertExists()
        compose.runOnIdle { assertEquals(0, retries) }
        compose.onNodeWithText("Reintentar").performClick()
        compose.runOnIdle { assertEquals(1, retries) }
    }

    @Test fun bufferingHasAVisibleStatus() {
        compose.setContent {
            MaterialTheme { PlaybackStatusBanner(AudioPlayerState(isConnecting = false, isBuffering = true)) {} }
        }
        compose.onNodeWithText("Preparando audio…").assertExists()
    }

    @Test fun waitingTimerCanBeCancelledWithoutFakeMinuteCount() {
        var cancelled = 0
        compose.setContent {
            MaterialTheme { SleepTimerDialog(0, { _, _ -> }, { cancelled++ }, {},
                SleepTimerState.WaitingForTrackEnd) }
        }
        compose.onNodeWithText("Esperando a que termine la canción").assertExists()
        compose.onNodeWithText("Tiempo restante: 1 min").assertDoesNotExist()
        compose.onNodeWithText("Cancelar Timer").performClick()
        compose.runOnIdle { assertEquals(1, cancelled) }
    }

    @Test fun playerMenuInspectsTheSelectedSongOnlyOnRequest() {
        var inspectedId: Long? = null
        compose.setContent {
            MaterialTheme {
                FullPlayerSheet(playerState = AudioPlayerState(currentSong = song, isConnecting = false),
                    isFavorite = false, onToggleFavorite = {}, onDismiss = {}, onTogglePlayPause = {},
                    onSeekTo = {}, onNext = {}, onPrevious = {}, onRewind = {}, onForward = {},
                    onToggleShuffle = {}, onEditTags = { _, _, _ -> }, inspectAudio = {
                        inspectedId = it.id
                        com.example.comarleyaetheraudio.domain.playback.AudioInspection("audio/mpeg", 44100, 2)
                    })
            }
        }
        compose.runOnIdle { assertEquals(null, inspectedId) }
        compose.onNodeWithContentDescription("Opciones").performClick()
        compose.onNodeWithText("Información del archivo").performClick()
        compose.onNodeWithText("Códec: audio/mpeg").assertExists()
        compose.onNodeWithText("Frecuencia: 44100 Hz").assertExists()
        compose.onNodeWithText("Profundidad de bits: No disponible").assertExists()
        compose.runOnIdle { assertEquals(song.id, inspectedId) }
    }
}
