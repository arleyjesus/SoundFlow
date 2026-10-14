package com.example.comarleyaetheraudio.domain

import com.example.comarleyaetheraudio.domain.library.*
import org.junit.Assert.assertEquals
import org.junit.Test

class LibraryOrderTest {
    private data class Track(val id: Long, val title: String, val artist: String, val date: Long = 0)
    private val tracks = listOf(Track(3, "Árbol", "Zeta"), Track(9, "Barco", "Ana"), Track(1, "arbol", "Ana"))
    private fun ids(order: LibraryOrder) = orderedLibrary(tracks, order, Track::id, Track::title, Track::artist).map { it.id }
    @Test fun recentOrderUsesIdentityRatherThanTitle() { assertEquals(listOf(9L, 3L, 1L), ids(LibraryOrder.ADDED_NEWEST)) }
    @Test fun oldestOrderReversesRecentOrder() { assertEquals(listOf(1L, 3L, 9L), ids(LibraryOrder.ADDED_OLDEST)) }
    @Test fun titleOrderFoldsAccentsAndBreaksTiesById() { assertEquals(listOf(1L, 3L, 9L), ids(LibraryOrder.TITLE)) }
    @Test fun artistOrderThenUsesTitle() { assertEquals(listOf(1L, 9L, 3L), ids(LibraryOrder.ARTIST)) }
    @Test fun sortingDoesNotMutateSource() { ids(LibraryOrder.TITLE); assertEquals(listOf(3L, 9L, 1L), tracks.map { it.id }) }
    private val dated = listOf(Track(90, "Old", "A", 1000), Track(2, "Today", "A", 3000),
        Track(100, "Unknown", "A"), Track(3, "Yesterday", "A", 2000), Track(4, "Tie", "A", 2000))
    private fun datedIds(order: LibraryOrder) = orderedLibrary(dated, order, Track::id, Track::title, Track::artist, Track::date).map { it.id }
    @Test fun newestUsesDateRatherThanImportId() { assertEquals(listOf(2L, 4L, 3L, 90L, 100L), datedIds(LibraryOrder.ADDED_NEWEST)) }
    @Test fun oldestKeepsUnknownDatesAtEndAndTiesStable() { assertEquals(listOf(90L, 3L, 4L, 2L, 100L), datedIds(LibraryOrder.ADDED_OLDEST)) }
}
