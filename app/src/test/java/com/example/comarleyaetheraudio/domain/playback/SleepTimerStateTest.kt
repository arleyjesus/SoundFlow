package com.example.comarleyaetheraudio.domain.playback

import org.junit.Assert.*
import org.junit.Test

class SleepTimerStateTest {
    @Test fun roundsRemainingSecondsUpToMinutes() {
        assertEquals(SleepTimerState.CountingDown(2), SleepTimerState.countdown(61))
        assertEquals(SleepTimerState.CountingDown(1), SleepTimerState.countdown(60))
        assertEquals(SleepTimerState.CountingDown(1), SleepTimerState.countdown(1))
    }
    @Test fun waitingIsActiveWithoutInventingRemainingMinutes() {
        assertTrue(SleepTimerState.WaitingForTrackEnd.isActive)
        assertEquals(0, SleepTimerState.WaitingForTrackEnd.remainingMinutes)
        assertFalse(SleepTimerState.Off.isActive)
    }
    @Test fun expiredCountdownStaysActiveWhileStopping() {
        assertEquals(SleepTimerState.Stopping, SleepTimerState.countdown(0))
        assertTrue(SleepTimerState.Stopping.isActive)
    }
    @Test fun roundingDoesNotOverflow() {
        assertEquals(35_791_395, SleepTimerState.countdown(Int.MAX_VALUE).remainingMinutes)
    }
}
