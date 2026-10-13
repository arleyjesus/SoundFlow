package com.example.comarleyaetheraudio.data.local.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ArtworkSizingTest {
    @Test fun smallArtworkIsNotUpscaled() {
        assertEquals(1, ArtworkSizing.sampleSize(120, 200, 250))
    }
    @Test fun largeSquareIsSampledBeforeAllocation() {
        assertEquals(16, ArtworkSizing.sampleSize(4000, 4000, 250))
    }
    @Test fun bothPortraitAndLandscapeRespectTheLongestSide() {
        assertEquals(32, ArtworkSizing.sampleSize(100, 8000, 250))
        assertEquals(32, ArtworkSizing.sampleSize(8000, 100, 250))
    }
    @Test fun oddDimensionsUseCeilingDivision() {
        assertEquals(4, ArtworkSizing.sampleSize(501, 251, 250))
    }
    @Test fun largeDimensionsDoNotOverflow() {
        val sample = ArtworkSizing.sampleSize(Int.MAX_VALUE, Int.MAX_VALUE, 256)
        assertTrue(sample > 0)
        assertTrue((Int.MAX_VALUE.toLong() + sample - 1) / sample <= 256)
    }
}
