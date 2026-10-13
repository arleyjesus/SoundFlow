package com.example.comarleyaetheraudio.presentation.settings

import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import com.example.comarleyaetheraudio.data.player.AudioPlayerHandler

@Composable
fun AudioFxScreen(playerHandler: AudioPlayerHandler, onBackClick: () -> Unit) {
    val state by playerHandler.audioEffectsState.collectAsStateWithLifecycle()
    AudioFxPanel(state, playerHandler::setAudioEffectsEnabled,
        playerHandler::setEqualizerBandLevelMb, playerHandler::setBassBoostStrength,
        playerHandler::setVirtualizerStrength, playerHandler::resetAudioEffects,
        playerHandler::refreshAudioEffects, onBackClick)
}
