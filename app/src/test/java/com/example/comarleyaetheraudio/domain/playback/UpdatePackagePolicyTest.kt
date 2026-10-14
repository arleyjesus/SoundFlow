package com.example.comarleyaetheraudio.domain.playback

import org.junit.Assert.*
import org.junit.Test

class UpdatePackagePolicyTest {
    private val url = "https://github.com/arleyjesus/SoundFlow/releases/download/v4.0.0/SoundFlow-4.0.0.apk"
    @Test fun onlyOfficialHttpsReleaseApkIsAccepted() {
        assertTrue(UpdatePackagePolicy.officialAssetUrl(url))
        listOf(url.replace("https", "http"), url.replace("arleyjesus", "attacker"),
            url.replace("github.com", "github.com.evil.test"), "$url?redirect=x", url.replace(".apk", ".zip"),
            url.replace("github.com", "user@github.com")).forEach { assertFalse(it, UpdatePackagePolicy.officialAssetUrl(it)) }
    }
    @Test fun redirectsRejectUntrustedHostsAndDowngrade() {
        assertTrue(UpdatePackagePolicy.allowedDownloadUrl("https://release-assets.githubusercontent.com/file?token=example"))
        assertFalse(UpdatePackagePolicy.allowedDownloadUrl("http://release-assets.githubusercontent.com/file"))
        assertFalse(UpdatePackagePolicy.allowedDownloadUrl("https://evil.test/file.apk"))
    }
    private fun error(pkg: String = "soundflow", version: Long = 41, name: String = "4.0.0",
        signers: Set<String> = setOf("certificate")) = UpdatePackagePolicy.packageError(pkg, "soundflow", version,
        40, name, "v4.0.0", signers, setOf("certificate"))
    @Test fun matchingSignedNewerApkCanUpdate() { assertNull(error()) }
    @Test fun wrongApplicationCannotUpdate() { assertNotNull(error(pkg = "other.app")) }
    @Test fun sameOrLowerCodeCannotUpdate() { assertNotNull(error(version = 40)); assertNotNull(error(version = 39)) }
    @Test fun releaseAndApkVersionMustMatch() { assertNotNull(error(name = "3.7.0")) }
    @Test fun changedOrMissingSignerCannotUpdate() { assertNotNull(error(signers = setOf("other"))); assertNotNull(error(signers = emptySet())) }
}
