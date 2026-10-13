package com.example.comarleyaetheraudio.data.remote

import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.CancellationException
import org.junit.Assert.*
import org.junit.Test

class BoundedHttpReaderTest {
    private class FakeConnection(val body: ByteArray, val length: Long = -1, val code: Int = 200) :
        HttpURLConnection(URL("https://soundflow.test/image")) {
        var disconnected = false
        var closed = false
        override fun connect() = Unit
        override fun usingProxy() = false
        override fun disconnect() { disconnected = true }
        override fun getResponseCode() = code
        override fun getContentLengthLong() = length
        override fun getInputStream(): InputStream = object : ByteArrayInputStream(body) {
            override fun close() { closed = true; super.close() }
        }
    }
    private val url = URL("https://soundflow.test/image")

    @Test fun acceptsExactlyTheLimitAndClosesResources() {
        val connection = FakeConnection(byteArrayOf(1, 2, 3))
        assertArrayEquals(connection.body, BoundedHttpReader({ connection }).read(url, 3))
        assertTrue(connection.closed && connection.disconnected)
        assertEquals(5000, connection.connectTimeout)
        assertEquals(5000, connection.readTimeout)
        assertFalse(connection.instanceFollowRedirects)
    }
    @Test fun rejectsUnknownLengthOverflowWithoutReturningPartialData() {
        val connection = FakeConnection(ByteArray(4))
        try { BoundedHttpReader({ connection }).read(url, 3); fail("Expected overflow") }
        catch (_: IOException) { assertTrue(connection.closed && connection.disconnected) }
    }
    @Test fun rejectsAdvertisedOversizedBodyBeforeOpeningStream() {
        val connection = FakeConnection(ByteArray(10), 10)
        try { BoundedHttpReader({ connection }).read(url, 3); fail("Expected overflow") }
        catch (_: IOException) { assertTrue(connection.disconnected); assertFalse(connection.closed) }
    }
    @Test fun rejectsRedirectRatherThanBypassingTheHttpsCheck() {
        val connection = FakeConnection(ByteArray(0), code = 302)
        try { BoundedHttpReader({ connection }).read(url, 10); fail("Expected rejection") }
        catch (_: IOException) { assertTrue(connection.disconnected) }
    }
    @Test fun rejectsPlainHttpWithoutOpeningAConnection() {
        var opened = false
        try {
            BoundedHttpReader({ opened = true; FakeConnection(ByteArray(0)) }).read(URL("http://soundflow.test"), 10)
            fail("Expected HTTPS requirement")
        } catch (_: IllegalArgumentException) { assertFalse(opened) }
    }
    @Test fun cancellationPropagatesAndDisconnects() {
        val connection = FakeConnection(ByteArray(10))
        var checks = 0
        try {
            BoundedHttpReader({ connection }).read(url, 20) { if (++checks == 3) throw CancellationException() }
            fail("Expected cancellation")
        } catch (_: CancellationException) { assertTrue(connection.disconnected && connection.closed) }
    }
    @Test fun totalTimeBudgetStopsEvenAStreamThatKeepsResponding() {
        val connection = FakeConnection(ByteArray(0))
        var nanos = 0L
        try {
            BoundedHttpReader({ connection }, { nanos.also { nanos += 4_000_000_000 } }).read(url, 20)
            fail("Expected deadline")
        } catch (_: IOException) { assertTrue(connection.disconnected) }
    }
}
