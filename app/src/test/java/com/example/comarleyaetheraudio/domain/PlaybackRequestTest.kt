package com.example.comarleyaetheraudio.domain

import com.example.comarleyaetheraudio.domain.playback.playbackRequest
import org.junit.Assert.*
import org.junit.Test

class PlaybackRequestTest {
    @Test fun selectionKeepsVisibleDescendingOrder() {
        val request = playbackRequest(4, listOf(9, 4, 2))
        assertEquals(listOf(9L, 4L, 2L), request.ids); assertEquals(1, request.startIndex); assertFalse(request.shuffle)
    }
    @Test fun filteredContextDoesNotIncludeUnseenTracks() { assertEquals(listOf(7L, 2L), playbackRequest(2, listOf(7, 2)).ids) }
    @Test fun missingSelectionFallsBackToSingleSong() { assertEquals(listOf(5L), playbackRequest(5, listOf(1, 2)).ids) }
    @Test fun emptyContextStillPlaysSelectedSong() { assertEquals(0, playbackRequest(5, emptyList()).startIndex) }
    @Test fun duplicateIdsAreRemovedWithoutReordering() {
        val request = playbackRequest(2, listOf(3, 2, 3, 1, 2))
        assertEquals(listOf(3L, 2L, 1L), request.ids); assertEquals(1, request.startIndex)
    }
    @Test fun shuffleRequiresExplicitRequest() {
        assertTrue(playbackRequest(1, listOf(1, 2), true).shuffle)
        assertFalse(playbackRequest(1, listOf(1, 2)).shuffle)
    }
}
