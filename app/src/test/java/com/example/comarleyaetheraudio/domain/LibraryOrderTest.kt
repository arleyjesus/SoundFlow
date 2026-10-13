package com.example.comarleyaetheraudio.domain

import com.example.comarleyaetheraudio.domain.library.*
import org.junit.Assert.assertEquals
import org.junit.Test

class LibraryOrderTest {
    private data class Track(val id: Long, val title: String, val artist: String)
    private val tracks = listOf(Track(3, "Árbol", "Zeta"), Track(9, "Barco", "Ana"), Track(1, "arbol", "Ana"))
    private fun ids(order: LibraryOrder) = orderedLibrary(tracks, order, Track::id, Track::title, Track::artist).map { it.id }
    @Test fun recentOrderUsesIdentityRatherThanTitle() { assertEquals(listOf(9L, 3L, 1L), ids(LibraryOrder.ADDED_NEWEST)) }
    @Test fun oldestOrderReversesRecentOrder() { assertEquals(listOf(1L, 3L, 9L), ids(LibraryOrder.ADDED_OLDEST)) }
    @Test fun titleOrderFoldsAccentsAndBreaksTiesById() { assertEquals(listOf(1L, 3L, 9L), ids(LibraryOrder.TITLE)) }
    @Test fun artistOrderThenUsesTitle() { assertEquals(listOf(1L, 9L, 3L), ids(LibraryOrder.ARTIST)) }
    @Test fun sortingDoesNotMutateSource() { ids(LibraryOrder.TITLE); assertEquals(listOf(3L, 9L, 1L), tracks.map { it.id }) }
}
