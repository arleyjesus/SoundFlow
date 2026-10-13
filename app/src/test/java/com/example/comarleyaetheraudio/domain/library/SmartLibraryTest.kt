package com.example.comarleyaetheraudio.domain.library

import org.junit.Assert.*
import org.junit.Test

class SmartLibraryTest {
    private fun track(id: Long, artist: String = "Artista $id", title: String = "Canción $id",
        duration: Long = 180000, source: String = "content://local/$id", genre: String = "Pop") =
        SmartTrack(id, title, artist, "Álbum", genre, duration, source)

    @Test fun sessionsNeverInventIdsAndKeepSeedFirstWithLimit() {
        val tracks = (1L..100L).map { track(it) }
        val session = suggestSession(tracks, setOf(1, 2), SessionStyle.VARIETY, seedId = 50, limit = 30)
        assertEquals(30, session.size)
        assertEquals(50L, session.first())
        assertEquals(session.size, session.distinct().size)
        assertTrue(session.all { id -> tracks.any { it.id == id } })
    }

    @Test fun favoritesAndDiscoveryRespectTheirSelectionAndCanBeEmpty() {
        val tracks = (1L..4L).map { track(it) }
        assertEquals(listOf(2L, 4L), suggestSession(tracks, setOf(2, 4), SessionStyle.FAVORITES))
        assertEquals(listOf(1L, 3L), suggestSession(tracks, setOf(2, 4), SessionStyle.DISCOVERY))
        assertTrue(suggestSession(tracks, emptySet(), SessionStyle.FAVORITES).isEmpty())
        assertTrue(suggestSession(emptyList(), emptySet(), SessionStyle.VARIETY).isEmpty())
    }

    @Test fun varietyAlternatesArtistsAndAvoidsRepeatingExactSource() {
        val tracks = listOf(track(1, "A"), track(2, "A"), track(3, "B"), track(4, "C", source = "content://local/1"))
        assertEquals(listOf(1L, 3L, 2L), suggestSession(tracks, emptySet(), SessionStyle.VARIETY))
    }

    @Test fun knownSeedGenreHasPriorityAndUnknownGenreDoesNotCreateSimilarity() {
        val tracks = listOf(track(1, genre = "Rock"), track(2), track(3))
        assertEquals(listOf(3L, 2L, 1L), suggestSession(tracks, emptySet(), SessionStyle.VARIETY, seedId = 3))
        assertEquals(listOf(2L, 1L), suggestSession(listOf(track(1, genre = "Unknown"), track(2, genre = "Unknown")),
            emptySet(), SessionStyle.VARIETY, seedId = 2))
    }

    @Test fun duplicateCandidatesNormalizeWhitespaceAndCaseButRequireKnownArtistAndDuration() {
        val tracks = listOf(track(1, "Artist", "Song"), track(2, " ARTIST ", "  song  ", duration = 181000),
            track(3, "Artist", "Song", duration = 0), track(4, "Unknown", "Song"), track(5, "Unknown", "Song"))
        assertEquals(listOf(PossibleDuplicates(MatchKind.SIMILAR_METADATA, listOf(1, 2))), findPossibleDuplicates(tracks))
    }

    @Test fun durationClustersDoNotChainUnrelatedVersionsAndDifferentArtistsStaySeparate() {
        val tracks = listOf(track(1, "A", "Song", 180000), track(2, "A", "Song", 182000),
            track(3, "A", "Song", 184000), track(4, "B", "Song", 180000))
        assertEquals(listOf(1L, 2L), findPossibleDuplicates(tracks).single().ids)
    }

    @Test fun exactSourceIsReportedOnceWithoutAlsoClaimingMetadataMatch() {
        val tracks = listOf(track(1, "A", "Song"), track(2, "A", "Song", source = "content://local/1"))
        assertEquals(listOf(PossibleDuplicates(MatchKind.SAME_SOURCE, listOf(1, 2))), findPossibleDuplicates(tracks))
        assertTrue(findPossibleDuplicates(listOf(tracks[0], tracks[0])).isEmpty())
    }

    @Test(expected = IllegalArgumentException::class) fun rejectsUnboundedSessionSize() {
        suggestSession(emptyList(), emptySet(), SessionStyle.VARIETY, limit = 10000)
    }
}
