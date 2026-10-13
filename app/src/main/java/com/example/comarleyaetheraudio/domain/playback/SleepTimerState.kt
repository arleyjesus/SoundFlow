package com.example.comarleyaetheraudio.domain.playback

sealed interface SleepTimerState {
    data object Off : SleepTimerState
    data class CountingDown(val minutes: Int) : SleepTimerState {
        init { require(minutes > 0) }
    }
    data object WaitingForTrackEnd : SleepTimerState
    data object Stopping : SleepTimerState

    val remainingMinutes: Int get() = (this as? CountingDown)?.minutes ?: 0
    val isActive: Boolean get() = this != Off

    companion object {
        fun countdown(remainingSeconds: Int): SleepTimerState =
            if (remainingSeconds <= 0) Stopping
            else CountingDown(remainingSeconds / 60 + if (remainingSeconds % 60 > 0) 1 else 0)
    }
}
