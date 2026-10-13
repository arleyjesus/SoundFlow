package com.example.comarleyaetheraudio.domain.playback

import com.example.comarleyaetheraudio.domain.model.LyricLine

object SyncedLyricsParser {
    const val MAX_CHARACTERS = 256 * 1024
    private val timestamp = Regex("\\[(\\d{1,4}):([0-5]\\d)(?:\\.(\\d{1,3}))?]")

    fun parse(content: String): List<LyricLine> {
        if (content.length > MAX_CHARACTERS) return emptyList()
        val lines = mutableListOf<LyricLine>()
        content.lineSequence().forEach { line ->
            val matches = timestamp.findAll(line).toList()
            val text = matches.lastOrNull()?.let { line.substring(it.range.last + 1).trim() }.orEmpty()
            if (text.isNotEmpty()) matches.forEach { match ->
                val milliseconds = match.groupValues[3].padEnd(3, '0').toLong()
                val time = match.groupValues[1].toLong() * 60_000 + match.groupValues[2].toLong() * 1000 + milliseconds
                lines.add(LyricLine(time, text))
            }
        }
        return lines.sortedBy { it.timeMs }
    }
}
