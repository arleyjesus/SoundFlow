package com.example.comarleyaetheraudio.domain.library

import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Test

class SearchLibraryTest {
    @Test fun blankQueryProducesNoResults() = runBlocking {
        assertTrue(searchLibrary(listOf("One"), "  ") { _, _ -> error("Blank query must not inspect items") }.isEmpty())
    }
    @Test fun trimsQueryAndRetainsSourceOrder() = runBlocking {
        assertEquals(listOf("Beta", "Alpha"), searchLibrary(listOf("Beta", "Echo", "Alpha"), " a ") { item, text -> item.contains(text, true) })
    }
    @Test fun emptyLibraryAndAbsentMatchProduceNoResults() = runBlocking {
        assertTrue(searchLibrary(emptyList<String>(), "a") { _, _ -> true }.isEmpty())
        assertTrue(searchLibrary(listOf("one"), "z") { item, text -> item.contains(text) }.isEmpty())
    }
    @Test fun obsoleteLargeQueryStopsCooperatively() = runBlocking {
        var inspected = 0
        val task = async(Dispatchers.Default) {
            val job = currentCoroutineContext().job
            searchLibrary((0 until 100_000).toList(), "query") { _, _ ->
                if (++inspected == 10) job.cancel()
                true
            }
        }
        try { task.await(); fail("Expected cancellation") } catch (_: CancellationException) { }
        assertTrue(inspected <= 64)
    }
}
