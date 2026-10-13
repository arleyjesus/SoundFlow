package com.example.comarleyaetheraudio.domain

import com.example.comarleyaetheraudio.domain.library.*
import org.junit.Assert.*
import org.junit.Test

class ListeningTrackerTest {
    @Test fun creditsOnceAfterThirtySeconds() {
        val tracker = ListeningTracker()
        val credits = (0L..60).mapNotNull { tracker.sample(1, it * 1000, 180_000, true, it * 1000) }
        assertEquals(listOf(1L), credits)
    }
    @Test fun shorterTrackCountsAfterHalf() {
        val tracker = ListeningTracker()
        val credits = (0L..10).mapNotNull { tracker.sample(2, it * 1000, 20_000, true, it * 1000) }
        assertEquals(listOf(2L), credits)
    }
    @Test fun seeksDoNotCountAsListening() {
        val tracker = ListeningTracker()
        assertNull(tracker.sample(1, 0, 180_000, true, 0))
        assertNull(tracker.sample(1, 90_000, 180_000, true, 1000))
        assertNull(tracker.sample(1, 91_000, 180_000, true, 2000))
    }
    @Test fun pauseDoesNotAccumulateWallTime() {
        val tracker = ListeningTracker()
        tracker.sample(1, 0, 180_000, true, 0)
        tracker.sample(1, 1000, 180_000, false, 1000)
        assertNull(tracker.sample(1, 1000, 180_000, true, 60_000))
    }
    @Test fun transitionDiscardsPreviousTracksTime() {
        val tracker = ListeningTracker()
        (0L..29).forEach { tracker.sample(1, it * 1000, 180_000, true, it * 1000) }
        assertNull(tracker.sample(2, 1000, 180_000, true, 30_000))
    }
    @Test fun repeatTransitionAllowsNewCredit() {
        val tracker = ListeningTracker()
        repeat(2) {
            tracker.reset()
            assertEquals(listOf(1L), (0L..30).mapNotNull { t -> tracker.sample(1, t * 1000, 180_000, true, t * 1000) })
        }
    }
    @Test fun malformedCountsAreIgnored() { assertEquals(mapOf(1L to 3), decodeListeningCounts("x;1:3;2:-1;3:abc;4:1000001")) }
    @Test fun countsRoundTripAndRemainBounded() {
        val encoded = encodeListeningCounts((1L..2500L).associateWith { 2 })
        assertEquals(2000, decodeListeningCounts(encoded).size)
        assertEquals(mapOf(8L to 7), decodeListeningCounts(encodeListeningCounts(mapOf(8L to 7))))
    }
}
