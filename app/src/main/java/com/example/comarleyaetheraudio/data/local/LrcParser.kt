package com.example.comarleyaetheraudio.data.local

import com.example.comarleyaetheraudio.domain.model.LyricLine
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import java.io.File

object LrcParser {
    fun parseLrcForSong(songPath: String): List<LyricLine> {
        // 1. Intentar leer archivo .lrc externo con el mismo nombre
        val lrcPath = songPath.substringBeforeLast(".") + ".lrc"
        val lrcFile = File(lrcPath)

        if (lrcFile.exists()) {
            val content = lrcFile.readText()
            val parsed = parseLrcContent(content)
            if (parsed.isNotEmpty()) return parsed
        }

        // 2. Intentar leer etiquetas ID3/USLT incrustadas dentro del propio archivo audio
        try {
            val audioFile = AudioFileIO.read(File(songPath))
            val tag = audioFile.tag
            if (tag != null) {
                val embeddedLyrics = tag.getFirst(FieldKey.LYRICS)
                if (embeddedLyrics.isNotBlank()) {
                    val parsed = parseLrcContent(embeddedLyrics)
                    if (parsed.isNotEmpty()) return parsed
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 3. Si no hay letra local ni incrustada, retorna lista vacía (sin textos genéricos de prueba)
        return emptyList()
    }

    private fun parseLrcContent(content: String): List<LyricLine> {
        val lines = mutableListOf<LyricLine>()
        val regex = "\\[(\\d{2}):(\\d{2})\\.(\\d{2,3})\\](.*)".toRegex()

        content.lines().forEach { line ->
            val match = regex.find(line)
            if (match != null) {
                val (min, sec, ms, text) = match.destructured
                val totalMs = (min.toLong() * 60 * 1000) + (sec.toLong() * 1000) + ms.toLong()
                if (text.trim().isNotEmpty()) {
                    lines.add(LyricLine(totalMs, text.trim()))
                }
            }
        }
        return lines.sortedBy { it.timeMs }
    }
}