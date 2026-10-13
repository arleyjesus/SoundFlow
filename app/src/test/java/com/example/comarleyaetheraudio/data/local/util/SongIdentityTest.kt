package com.example.comarleyaetheraudio.data.local.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class SongIdentityTest {
    @Test
    fun tenThousandDocumentAliasesStayStableAcrossTreesAndRepeatedScans() {
        val first = (0 until 10_000).map { songIdentity("content://provider/tree/A/document/music%3A$it") }.toSet()
        val second = (0 until 10_000).map { songIdentity("content://provider/tree/B/document/music%3A$it") }.toSet()
        assertEquals(10_000, first.size)
        assertEquals(first, second)
        assertEquals(10_000, (first + second).size)
    }

    @Test
    fun sameDocumentUnderDifferentTreesHasSameIdentity() {
        assertEquals(
            songIdentity("content://provider/tree/rootA/document/primary%3AMusic%2Fa.mp3"),
            songIdentity("content://provider/tree/rootB/document/primary%3AMusic%2Fa.mp3")
        )
    }

    @Test
    fun directAndTreeDocumentHaveSameIdentity() {
        assertEquals(songIdentity("content://provider/document/42"), songIdentity("content://provider/tree/root/document/42"))
    }

    @Test
    fun sameIdFromDifferentProvidersStaysDistinct() {
        assertNotEquals(songIdentity("content://providerA/document/42"), songIdentity("content://providerB/document/42"))
    }

    @Test
    fun documentIdsRemainCaseSensitiveAndOpaque() {
        assertNotEquals(songIdentity("content://provider/document/A"), songIdentity("content://provider/document/a"))
        assertNotEquals(songIdentity("content://provider/document/a%2F..%2Fb"), songIdentity("content://provider/document/b"))
    }

    @Test
    fun literalPlusIsNotAFormEncodedSpace() {
        assertEquals(songIdentity("content://provider/document/a+b"), songIdentity("content://provider/document/a%2Bb"))
        assertNotEquals(songIdentity("content://provider/document/a+b"), songIdentity("content://provider/document/a%20b"))
    }

    @Test
    fun percentEscapesAreDecodedOnlyOnce() {
        assertNotEquals(songIdentity("content://provider/document/a%252Fb"), songIdentity("content://provider/document/a%2Fb"))
        assertEquals(SongIdentity.Document("provider", "a%2Fb"), songIdentity("content://provider/document/a%252Fb"))
    }

    @Test
    fun equivalentUtf8AndHexEncodingsHaveSameIdentity() {
        assertEquals(songIdentity("content://provider/document/Canci%C3%B3n%3A1"), songIdentity("content://provider/tree/root/document/Canci%c3%b3n%3a1"))
    }

    @Test
    fun mediaStoreAndOtherSchemesUseExactUri() {
        val mediaStore = "content://media/external/audio/media/42"
        assertEquals(SongIdentity.ExactUri(mediaStore), songIdentity(mediaStore))
        assertNotEquals(songIdentity(mediaStore), songIdentity("content://provider/document/42"))
        val file = "file:///Music/a.mp3"
        assertEquals(SongIdentity.ExactUri(file), songIdentity(file))
    }

    @Test
    fun queriesFragmentsTreeRootsAndUnsupportedPathsAreNotCanonicalized() {
        listOf(
            "content://provider/document/42?variant=1",
            "content://provider/document/42#part",
            "content://provider/tree/root",
            "content://provider/tree/root/document/42/children",
            "content://provider/document/"
        ).forEach { assertEquals(SongIdentity.ExactUri(it), songIdentity(it)) }
    }

    @Test
    fun malformedOrUndecodableValuesFallBackToExactUri() {
        listOf("not a uri", "content://provider/document/%ZZ", "content://provider/document/%FF")
            .forEach { assertEquals(SongIdentity.ExactUri(it), songIdentity(it)) }
    }
}
