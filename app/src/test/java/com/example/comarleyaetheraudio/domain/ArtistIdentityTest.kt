package com.example.comarleyaetheraudio.domain

import com.example.comarleyaetheraudio.domain.library.sameArtistName
import org.junit.Assert.*
import org.junit.Test

class ArtistIdentityTest {
    @Test fun caseAndWhitespaceMatch() { assertTrue(sameArtistName("  Rayo   & Toby ", "rayo & toby")) }
    @Test fun partialNamesDoNotMatch() { assertFalse(sameArtistName("Queen", "Queens of the Stone Age")) }
    @Test fun collaborationsDoNotBecomeSoloPortraits() { assertFalse(sameArtistName("A & B", "A")) }
    @Test fun unknownArtistsNeverMatch() { assertFalse(sameArtistName("<unknown>", "<unknown>")) }
    @Test fun accentsAreNotSilentlyDifferentArtists() { assertFalse(sameArtistName("Jose", "José")) }
}
