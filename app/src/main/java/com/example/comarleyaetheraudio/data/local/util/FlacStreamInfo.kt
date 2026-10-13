package com.example.comarleyaetheraudio.data.local.util

import com.example.comarleyaetheraudio.domain.playback.AudioInspection
import java.io.InputStream

/** Reads only the 42-byte native FLAC prefix, never the track or embedded cover. RFC 9639 §8.2. */
object FlacStreamInfo {
    fun read(input: InputStream): AudioInspection? {
        val header = ByteArray(42)
        for (i in header.indices) {
            val next = input.read()
            if (next < 0) return null
            header[i] = next.toByte()
        }
        fun byte(i: Int) = header[i].toInt() and 0xff
        if (byte(0) != 0x66 || byte(1) != 0x4c || byte(2) != 0x61 || byte(3) != 0x43 ||
            (byte(4) and 0x7f) != 0 || byte(5) != 0 || byte(6) != 0 || byte(7) != 34) return null
        val minBlock = (byte(8) shl 8) or byte(9)
        val maxBlock = (byte(10) shl 8) or byte(11)
        val rate = (byte(18) shl 12) or (byte(19) shl 4) or (byte(20) ushr 4)
        val channels = ((byte(20) ushr 1) and 7) + 1
        val bits = (((byte(20) and 1) shl 4) or (byte(21) ushr 4)) + 1
        if (minBlock < 16 || maxBlock < minBlock || rate !in 1..1048575 || bits !in 4..32) return null
        return AudioInspection("audio/flac", rate, channels, bits,
            detail = "Cabecera FLAC STREAMINFO. No verifica el origen de la grabación ni todos los fotogramas.")
    }
}
