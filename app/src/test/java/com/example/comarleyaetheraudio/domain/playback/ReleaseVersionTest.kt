package com.example.comarleyaetheraudio.domain.playback

import org.junit.Assert.*
import org.junit.Test

class ReleaseVersionTest {
    @Test fun differentOlderVersionIsNotAnUpdate() {
        assertFalse(ReleaseVersion.isNewer("v3.6.9", "3.7.0"))
        assertFalse(ReleaseVersion.isNewer("v3.7.0", "3.7.0"))
    }
    @Test fun stableVersionsCompareNumerically() {
        assertTrue(ReleaseVersion.isNewer("v3.10.0", "3.9.9"))
        assertTrue(ReleaseVersion.isNewer("4.0.0", "3.7.0"))
        assertTrue(ReleaseVersion.isNewer("3.7.1", "3.7.0"))
    }
    @Test fun malformedAndPreviewTagsAreNotOfferedAsStable() {
        for (tag in listOf("", "latest", "4.0.0-beta", "4.0", "-4.0.0", "999999999999.0.0")) {
            assertFalse(ReleaseVersion.isNewer(tag, "3.7.0"))
        }
    }
    @Test fun unknownCurrentVersionDoesNotTriggerAnUpdate() {
        assertFalse(ReleaseVersion.isNewer("4.0.0", "development"))
    }
}
