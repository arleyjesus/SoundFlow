package com.example.comarleyaetheraudio.domain.playback

/** Only stable numeric releases can trigger an update; a different string is not necessarily newer. */
object ReleaseVersion {
    private fun parts(version: String): List<Int>? {
        val match = Regex("^v?(\\d+)\\.(\\d+)\\.(\\d+)$").matchEntire(version.trim()) ?: return null
        val values = match.groupValues.drop(1).map { it.toIntOrNull() ?: return null }
        return values
    }

    fun isNewer(candidate: String, current: String): Boolean {
        val next = parts(candidate) ?: return false
        val previous = parts(current) ?: return false
        for (i in next.indices) if (next[i] != previous[i]) return next[i] > previous[i]
        return false
    }
}
