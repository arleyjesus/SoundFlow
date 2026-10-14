package com.example.comarleyaetheraudio.domain.playback

import java.net.URI

object UpdatePackagePolicy {
    fun officialAssetUrl(value: String): Boolean = try {
        val uri = URI(value)
        uri.scheme == "https" && uri.host == "github.com" && uri.userInfo == null &&
            (uri.port == -1 || uri.port == 443) && uri.rawQuery == null && uri.fragment == null &&
            uri.path.startsWith("/arleyjesus/SoundFlow/releases/download/") &&
            uri.path.endsWith(".apk", ignoreCase = true) && !uri.path.contains("/../")
    } catch (_: Exception) { false }

    fun allowedDownloadUrl(value: String): Boolean = try {
        val uri = URI(value)
        uri.scheme == "https" && uri.userInfo == null && (uri.port == -1 || uri.port == 443) &&
            uri.host in setOf("github.com", "release-assets.githubusercontent.com", "objects.githubusercontent.com")
    } catch (_: Exception) { false }

    fun packageError(actualPackage: String?, expectedPackage: String, actualVersion: Long,
        installedVersion: Long, actualName: String?, expectedName: String,
        actualSigners: Set<String>, installedSigners: Set<String>): String? = when {
        actualPackage != expectedPackage -> "El APK no pertenece a SoundFlow."
        actualVersion <= installedVersion -> "El APK no tiene un código de versión superior."
        actualName != expectedName.removePrefix("v") -> "La versión del APK no coincide con la publicación."
        actualSigners.isEmpty() || actualSigners != installedSigners ->
            "La firma del APK no coincide con tu instalación. No desinstales la app: podrías perder datos."
        else -> null
    }
}
