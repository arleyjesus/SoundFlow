package com.example.comarleyaetheraudio.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.comarleyaetheraudio.data.player.AudioPlayerHandler
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PlayerConnectionTest {
    @Test fun connectsAndReleasesIdempotentlyWithoutChangingPlaybackQueue() = runBlocking {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        lateinit var handler: AudioPlayerHandler
        instrumentation.runOnMainSync { handler = AudioPlayerHandler(instrumentation.targetContext) }
        try {
            withTimeout(10_000) {
                while (handler.playerState.value.isConnecting || handler.audioEffectsState.value.isConnecting ||
                    handler.audioEffectsState.value.isUpdating) delay(50)
            }
            assertNull(handler.playerState.value.errorMessage)
            assertNull(handler.audioEffectsState.value.errorMessage)
        } finally {
            instrumentation.runOnMainSync { handler.release(); handler.release() }
        }
        assertFalse(handler.playerState.value.isConnecting)
    }
}
