package com.example.comarleyaetheraudio.data

import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.comarleyaetheraudio.data.local.util.AndroidAudioInspector
import com.example.comarleyaetheraudio.domain.playback.AudioCompression
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

@RunWith(AndroidJUnit4::class)
class AudioInspectorTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test fun readsPcmWaveWithoutModifyingIt() = runBlocking {
        val file = File.createTempFile("soundflow-inspector-", ".wav", context.cacheDir)
        try {
            val bytes = ByteBuffer.allocate(1644).order(ByteOrder.LITTLE_ENDIAN).apply {
                put("RIFF".toByteArray()); putInt(1636); put("WAVEfmt ".toByteArray()); putInt(16)
                putShort(1); putShort(1); putInt(8000); putInt(16000); putShort(2); putShort(16)
                put("data".toByteArray()); putInt(1600)
            }.array()
            file.writeBytes(bytes)
            val info = AndroidAudioInspector(context).inspect(Uri.fromFile(file))
            assertEquals("audio/raw", info.codecMime)
            assertEquals(8000, info.sampleRateHz); assertEquals(1, info.channels)
            assertEquals(16, info.bitDepth); assertEquals(AudioCompression.LOSSLESS, info.compression)
            assertArrayEquals(bytes, file.readBytes())
        } finally { file.delete() }
    }

    @Test fun unavailableAndRemoteSourcesRemainUnknown() = runBlocking {
        val inspector = AndroidAudioInspector(context)
        for (uri in listOf("content://soundflow.invalid/missing", "https://example.invalid/audio.flac")) {
            val info = inspector.inspect(Uri.parse(uri))
            assertNull(info.sampleRateHz); assertNull(info.bitDepth)
            assertEquals(AudioCompression.UNKNOWN, info.compression)
        }
    }
}
