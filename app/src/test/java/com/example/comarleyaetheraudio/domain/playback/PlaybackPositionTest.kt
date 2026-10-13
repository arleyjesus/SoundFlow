package com.example.comarleyaetheraudio.domain.playback

import org.junit.Assert.assertEquals
import org.junit.Test

class PlaybackPositionTest {
    @Test fun unknownPlayerDurationUsesNonnegativeSongDuration() {
        assertEquals(60_000L, playbackDuration(Long.MIN_VALUE + 1, 60_000))
        assertEquals(0L, playbackDuration(-1, -100))
        assertEquals(12_000L, playbackDuration(12_000, 60_000))
    }

    @Test fun seekingNeverLeavesKnownTrackBounds() {
        assertEquals(0L, boundedPlaybackPosition(-10, 100))
        assertEquals(100L, boundedPlaybackPosition(500, 100))
        assertEquals(50L, boundedPlaybackPosition(50, 100))
    }

    @Test fun unknownDurationCannotProduceNegativeSeek() {
        assertEquals(0L, boundedPlaybackPosition(500, Long.MIN_VALUE + 1))
        assertEquals(0L, shiftedPlaybackPosition(500, 10_000, -1))
    }

    @Test fun forwardAndRewindStopAtTrackEnds() {
        assertEquals(60_000L, shiftedPlaybackPosition(55_000, 10_000, 60_000))
        assertEquals(0L, shiftedPlaybackPosition(5_000, -10_000, 60_000))
        assertEquals(25_000L, shiftedPlaybackPosition(15_000, 10_000, 60_000))
    }

    @Test fun extremeOffsetsDoNotOverflowIntoOppositeDirection() {
        assertEquals(Long.MAX_VALUE, shiftedPlaybackPosition(Long.MAX_VALUE - 1, Long.MAX_VALUE, Long.MAX_VALUE))
        assertEquals(0L, shiftedPlaybackPosition(50, Long.MIN_VALUE, 100))
    }
}
