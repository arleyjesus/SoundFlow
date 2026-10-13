package com.example.comarleyaetheraudio.data.local.util

import java.net.URI
import java.net.URISyntaxException
import java.net.URLDecoder

internal sealed interface SongIdentity {
    data class Document(val authority: String, val documentId: String) : SongIdentity
    data class ExactUri(val value: String) : SongIdentity
}

/** Identidad de comparacion; nunca sustituye la URI usada para abrir el archivo. */
internal fun songIdentity(value: String): SongIdentity {
    val exact = SongIdentity.ExactUri(value)
    val uri = try { URI(value) } catch (_: URISyntaxException) { return exact }
    val authority = uri.rawAuthority
    if (uri.scheme != "content" || authority.isNullOrEmpty() ||
        uri.rawQuery != null || uri.rawFragment != null
    ) return exact

    val segments = uri.rawPath?.split('/')?.drop(1) ?: return exact
    val encodedId = when {
        segments.size == 2 && segments[0] == "document" -> segments[1]
        segments.size == 4 && segments[0] == "tree" &&
            segments[1].isNotEmpty() && segments[2] == "document" -> segments[3]
        else -> return exact
    }
    if (encodedId.isEmpty()) return exact
    // Los IDs son opacos: decodificar una sola vez, sin interpretar rutas,
    // cambiar mayusculas ni convertir el signo + de un segmento en espacio.
    val documentId = try {
        URLDecoder.decode(encodedId.replace("+", "%2B"), "UTF-8")
    } catch (_: IllegalArgumentException) {
        return exact
    }
    if ('\uFFFD' in documentId) return exact
    return SongIdentity.Document(authority, documentId)
}
