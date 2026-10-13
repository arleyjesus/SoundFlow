package com.example.comarleyaetheraudio.data.remote

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/** Size and time budgets also apply to unknown/chunked Content-Length. */
class BoundedHttpReader(
    private val open: (URL) -> HttpURLConnection = { it.openConnection() as HttpURLConnection },
    private val nanoTime: () -> Long = System::nanoTime
) {
    fun read(url: URL, maxBytes: Int, checkActive: () -> Unit = {}): ByteArray {
        require(url.protocol == "https")
        require(maxBytes in 1..(4 * 1024 * 1024))
        val started = nanoTime()
        fun checkBudget() {
            checkActive()
            if ((nanoTime() - started) / 1_000_000 >= 10_000) throw IOException("HTTP time budget exceeded")
        }
        checkBudget()
        val connection = open(url)
        try {
            connection.requestMethod = "GET"
            connection.connectTimeout = 5_000
            connection.readTimeout = 5_000
            connection.instanceFollowRedirects = false
            if (connection.responseCode != HttpURLConnection.HTTP_OK) throw IOException("HTTP response rejected")
            checkBudget()
            if (connection.contentLengthLong > maxBytes) throw IOException("HTTP body too large")
            return connection.inputStream.use { input ->
                ByteArrayOutputStream(minOf(maxBytes, 8192)).use { output ->
                    val buffer = ByteArray(8192)
                    while (true) {
                        checkBudget()
                        val count = input.read(buffer, 0, minOf(buffer.size, maxBytes - output.size() + 1))
                        if (count < 0) break
                        if (output.size() + count > maxBytes) throw IOException("HTTP body too large")
                        output.write(buffer, 0, count)
                    }
                    checkBudget()
                    output.toByteArray()
                }
            }
        } finally { connection.disconnect() }
    }
}
