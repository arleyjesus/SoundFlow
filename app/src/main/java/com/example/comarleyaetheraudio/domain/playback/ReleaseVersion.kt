package com.example.comarleyaetheraudio.domain.playback

/** Only stable numeric releases can trigger an update; a different string is not necessarily newer. */
object ReleaseVersion {
    private fun parts(version: String, allowRc: Boolean = false): List<Int>? {
        val match = Regex("^v?(\\d+)\\.(\\d+)\\.(\\d+)(-rc\\.\\d+)?$").matchEntire(version.trim()) ?: return null
        if (!allowRc && match.groupValues[4].isNotEmpty()) return null
        val values = match.groupValues.slice(1..3).map { it.toIntOrNull() ?: return null }
        return values
    }

    fun isNewer(candidate: String, current: String): Boolean {
        val next = parts(candidate) ?: return false
        val previous = parts(current, allowRc = true) ?: return false
        for (i in next.indices) if (next[i] != previous[i]) return next[i] > previous[i]
        return current.trim().contains("-rc.")
    }
}
