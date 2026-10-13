package com.example.comarleyaetheraudio.data.local.util

import com.example.comarleyaetheraudio.domain.model.LyricLine
import com.example.comarleyaetheraudio.domain.playback.SyncedLyricsParser
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import java.io.File

object LrcParser {
    fun parseLrcForSong(songPath: String): List<LyricLine> {
        if (songPath.isBlank() || songPath.startsWith("content://")) return emptyList()
        // 1. Intentar leer archivo .lrc externo con el mismo nombre
        val lrcPath = songPath.substringBeforeLast(".") + ".lrc"
        val lrcFile = File(lrcPath)

        if (lrcFile.isFile && lrcFile.length() <= SyncedLyricsParser.MAX_CHARACTERS) {
            val content = try { lrcFile.reader().use { it.readTextLimited() } }
                catch (_: java.io.IOException) { "" }
                catch (_: SecurityException) { "" }
            val parsed = SyncedLyricsParser.parse(content)
            if (parsed.isNotEmpty()) return parsed
        }

        // 2. Intentar leer etiquetas ID3/USLT incrustadas dentro del propio archivo audio
        try {
            val audioFile = AudioFileIO.read(File(songPath))
            val tag = audioFile.tag
            if (tag != null) {
                val embeddedLyrics = tag.getFirst(FieldKey.LYRICS)
                if (embeddedLyrics.isNotBlank()) {
                    val parsed = SyncedLyricsParser.parse(embeddedLyrics)
                    if (parsed.isNotEmpty()) return parsed
                }
            }
        } catch (_: Exception) {
        }

        // 3. Si no hay letra local ni incrustada, retorna lista vacía (sin textos genéricos de prueba)
        return emptyList()
    }

    private fun java.io.Reader.readTextLimited(): String {
        val buffer = CharArray(SyncedLyricsParser.MAX_CHARACTERS + 1)
        var size = 0
        while (size < buffer.size) {
            val count = read(buffer, size, buffer.size - size)
            if (count < 0) break
            size += count
        }
        return if (size <= SyncedLyricsParser.MAX_CHARACTERS) String(buffer, 0, size) else ""
    }
}
