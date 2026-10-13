package com.example.comarleyaetheraudio.data.local.util

import java.nio.file.Files
import java.util.Locale
import org.junit.Assert.*
import org.junit.Test

class ArtistCachePolicyTest {
    @Test fun keyNormalizesCaseAndSurroundingSpaces() {
        assertEquals(ArtistCachePolicy.key(" Artista "), ArtistCachePolicy.key("artista"))
        assertEquals(64, ArtistCachePolicy.key("artista").length)
    }
    @Test fun oldHashCollisionDoesNotAliasArtists() {
        assertEquals("an".hashCode(), "c0".hashCode())
        assertNotEquals(ArtistCachePolicy.key("an"), ArtistCachePolicy.key("c0"))
    }
    @Test fun keyDoesNotDependOnDeviceLocale() {
        val previous = Locale.getDefault()
        val expected = ArtistCachePolicy.key("INDIGO")
        try { Locale.setDefault(Locale.forLanguageTag("tr-TR")); assertEquals(expected, ArtistCachePolicy.key("INDIGO")) }
        finally { Locale.setDefault(previous) }
    }
    @Test fun failuresExpireAndSuccessfulFetchClearsBackoff() {
        var now = 1000L
        val backoff = ArtistFailureBackoff(now = { now })
        backoff.recordFailure("one"); assertTrue(backoff.isBlocked("one"))
        now += 60_000; assertFalse(backoff.isBlocked("one"))
        backoff.recordFailure("one"); backoff.clear("one"); assertFalse(backoff.isBlocked("one"))
    }
    @Test fun negativeCacheHasAFixedCapacity() {
        val backoff = ArtistFailureBackoff(2)
        backoff.recordFailure("one"); backoff.recordFailure("two"); backoff.recordFailure("three")
        assertFalse(backoff.isBlocked("one")); assertTrue(backoff.isBlocked("two")); assertTrue(backoff.isBlocked("three"))
    }
    @Test fun trimmingPreservesCurrentFileAndUnrelatedFiles() {
        val directory = Files.createTempDirectory("soundflow-artist-policy").toFile()
        try {
            val old = java.io.File(directory, "artist_${ArtistCachePolicy.key("old")}.jpg").apply { writeBytes(ByteArray(8)); setLastModified(1) }
            val current = java.io.File(directory, "artist_${ArtistCachePolicy.key("new")}.jpg").apply { writeBytes(ByteArray(8)) }
            val unrelated = java.io.File(directory, "user.txt").apply { writeText("preserve") }
            val sidecar = java.io.File(directory, "${current.name}.new").apply { writeBytes(ByteArray(8)) }
            ArtistCachePolicy.trim(directory, current, 10)
            assertFalse(old.exists()); assertTrue(current.exists()); assertTrue(sidecar.exists())
            assertEquals("preserve", unrelated.readText())
        } finally { directory.deleteRecursively() }
    }
}
