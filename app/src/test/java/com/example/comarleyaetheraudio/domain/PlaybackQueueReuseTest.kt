package com.example.comarleyaetheraudio.domain

import com.example.comarleyaetheraudio.domain.playback.canReusePlaybackQueue
import org.junit.Assert.*
import org.junit.Test

class PlaybackQueueReuseTest {
    private data class Track(val id: Long, val uri: String, val title: String = "Song")
    private val queue = listOf(Track(1, "content://a"), Track(2, "content://b"))
    private fun reusable(request: List<Track> = queue, previous: List<Track> = queue,
        actual: List<Long> = queue.map { it.id }) =
        canReusePlaybackQueue(request, previous, actual.size, Track::id) { actual[it] }

    @Test fun unchangedQueueCanSeekWithoutRebuilding() { assertTrue(reusable()) }
    @Test fun reorderedVisibleListMustReplaceQueue() { assertFalse(reusable(request = queue.reversed())) }
    @Test fun changedSourceWithSameIdentityMustReplaceQueue() {
        assertFalse(reusable(request = listOf(queue[0].copy(uri = "content://replacement"), queue[1])))
    }
    @Test fun editedMetadataMustRefreshQueue() {
        assertFalse(reusable(request = listOf(queue[0].copy(title = "Edited"), queue[1])))
    }
    @Test fun externallyReorderedTimelineMustNotBeReused() { assertFalse(reusable(actual = listOf(2, 1))) }
    @Test fun externallyRemovedItemMustNotBeReused() { assertFalse(reusable(actual = listOf(1))) }
    @Test fun emptyQueueCannotBeReused() { assertFalse(reusable(emptyList(), emptyList(), emptyList())) }
}
