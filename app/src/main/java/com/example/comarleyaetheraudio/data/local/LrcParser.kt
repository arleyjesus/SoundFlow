package com.example.comarleyaetheraudio.data.local

import com.example.comarleyaetheraudio.domain.model.LyricLine
import java.io.File

object LrcParser {
    fun parseLrcForSong(songPath: String?): List<LyricLine> {
        if (songPath == null) return getMockLyrics() // Letras de prueba si no hay ruta

        return try {
            val songFile = File(songPath)
            val lrcFile = File(songFile.parent, "${songFile.nameWithoutExtension}.lrc")

            if (!lrcFile.exists()) return getMockLyrics() // Letras de prueba si no existe el archivo

            val lines = mutableListOf<LyricLine>()
            lrcFile.forEachLine { line ->
                val regex = "\\[(\\d{2}):(\\d{2})\\.(\\d{2,3})\\](.*)".toRegex()
                val match = regex.find(line)
                if (match != null) {
                    val (min, sec, ms, text) = match.destructured
                    val totalMs = (min.toLong() * 60 * 1000) + (sec.toLong() * 1000) + ms.toLong()
                    if (text.trim().isNotEmpty()) {
                        lines.add(LyricLine(totalMs, text.trim()))
                    }
                }
            }
            lines.sortedBy { it.timeMs }
        } catch (_: Exception) {
            getMockLyrics()
        }
    }

    // Datos de prueba para validar que la UI funciona correctamente
    private fun getMockLyrics(): List<LyricLine> {
        return listOf(
            LyricLine(0, "🎵 [Música]"),
            LyricLine(10000, "Esta es una letra de prueba sincronizada"),
            LyricLine(20000, "Si ves que el texto avanza solo..."),
            LyricLine(30000, "¡La UI de letras funciona perfecto!"),
            LyricLine(40000, "Para ver letras reales, añade un archivo .lrc"),
            LyricLine(50000, "con el mismo nombre que tu archivo .mp3")
        )
    }
}