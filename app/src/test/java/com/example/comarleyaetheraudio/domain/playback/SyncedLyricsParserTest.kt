package com.example.comarleyaetheraudio.domain.playback

import org.junit.Assert.*
import org.junit.Test

class SyncedLyricsParserTest {
    @Test fun fractionsUseTheirActualDecimalPrecision() {
        val lines = SyncedLyricsParser.parse("[00:01.5]A\n[00:01.50]B\n[00:01.500]C")
        assertEquals(listOf(1500L, 1500L, 1500L), lines.map { it.timeMs })
    }
    @Test fun twoDigitFractionsAreCentisecondsNotMilliseconds() {
        assertEquals(62340L, SyncedLyricsParser.parse("[01:02.34]Texto").single().timeMs)
    }
    @Test fun repeatedTimestampsReuseTextAndSortChronologically() {
        val lines = SyncedLyricsParser.parse("[02:00][00:30.125]Estribillo\n[00:01]Inicio")
        assertEquals(listOf(1000L, 30125L, 120000L), lines.map { it.timeMs })
        assertEquals(listOf("Inicio", "Estribillo", "Estribillo"), lines.map { it.text })
    }
    @Test fun metadataInvalidSecondsAndEmptyLinesAreIgnored() {
        assertTrue(SyncedLyricsParser.parse("[ar:Artista]\n[00:60.00]Inválido\n[00:01.5]   ").isEmpty())
    }
    @Test fun oversizedInputIsRejectedBeforeParsing() {
        assertTrue(SyncedLyricsParser.parse("[00:01.5]" + "x".repeat(SyncedLyricsParser.MAX_CHARACTERS)).isEmpty())
    }
}
