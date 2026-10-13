package com.example.comarleyaetheraudio.presentation

import android.net.Uri
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.comarleyaetheraudio.domain.model.Song
import com.example.comarleyaetheraudio.domain.playback.AudioInspection
import com.example.comarleyaetheraudio.presentation.player.AudioInspectorDialog
import kotlinx.coroutines.CompletableDeferred
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AudioInspectorDialogTest {
    @get:Rule val compose = createComposeRule()

    @Test fun replacingTheSongCancelsThePreviousInspection() {
        val first = Song(1, "Primera", "Artista", "Album", duration = 1000,
            contentUri = Uri.parse("content://soundflow.test/1"), albumArtUri = null, size = 1, mimeType = "audio/flac")
        val current = mutableStateOf(first)
        val pending = CompletableDeferred<AudioInspection>()
        var cancelled = false
        compose.setContent {
            MaterialTheme {
                AudioInspectorDialog(current.value, inspect = {
                    if (it.id == 1L) try { pending.await() } finally { cancelled = true }
                    else AudioInspection("audio/mpeg", 44100, 2)
                }, onDismiss = {})
            }
        }
        compose.onNodeWithText("Inspeccionando archivo…").assertExists()
        compose.runOnIdle { current.value = first.copy(id = 2, title = "Segunda", contentUri = Uri.parse("content://soundflow.test/2")) }
        compose.onNodeWithText("Códec: audio/mpeg").assertExists()
        compose.runOnIdle {
            assertTrue(cancelled)
            pending.complete(AudioInspection("audio/flac", 96000, 2, 24))
        }
        compose.onNodeWithText("Frecuencia: 96000 Hz").assertDoesNotExist()
    }
}
