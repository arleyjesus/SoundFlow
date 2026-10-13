package com.example.comarleyaetheraudio.domain.playback

import org.junit.Assert.*
import org.junit.Test

class ReleaseCandidateTest {
    @Test fun stableReleaseUpgradesMatchingRc() { assertTrue(ReleaseVersion.isNewer("4.0.0", "4.0.0-rc.1")) }
    @Test fun oldStableDoesNotDowngradeRc() { assertFalse(ReleaseVersion.isNewer("3.9.0", "4.0.0-rc.1")) }
    @Test fun rcIsNeverOfferedAsStableUpdate() { assertFalse(ReleaseVersion.isNewer("4.0.1-rc.1", "4.0.0")) }
}
