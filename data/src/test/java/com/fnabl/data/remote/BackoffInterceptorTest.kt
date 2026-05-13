package com.fnabl.data.remote

import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

class BackoffInterceptorTest {
    private val server = MockWebServer()
    private val json = Json { ignoreUnknownKeys = true }
    private var now: Long = 0L
    private val sleeps = mutableListOf<Long>()

    private val interceptor =
        BackoffInterceptor(
            json = json,
            clock = { now },
            sleeper = { ms ->
                sleeps += ms
                now += ms
            },
        )

    private val client =
        OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .readTimeout(1, TimeUnit.SECONDS)
            .build()

    @Before
    fun setUp() {
        server.start()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `response without backoff does not delay the next call`() {
        server.enqueue(jsonResponse("""{"items":[]}"""))
        server.enqueue(jsonResponse("""{"items":[]}"""))

        execute()
        execute()

        assertEquals(emptyList<Long>(), sleeps)
    }

    @Test
    fun `response with backoff delays the next call by that many seconds`() {
        server.enqueue(jsonResponse("""{"items":[],"backoff":3}"""))
        server.enqueue(jsonResponse("""{"items":[]}"""))

        execute()
        execute()

        assertEquals(listOf(3_000L), sleeps)
    }

    @Test
    fun `time already elapsed past the backoff window does not delay`() {
        server.enqueue(jsonResponse("""{"items":[],"backoff":2}"""))
        server.enqueue(jsonResponse("""{"items":[]}"""))

        execute()
        now += 5_000L
        execute()

        assertEquals(emptyList<Long>(), sleeps)
    }

    @Test
    fun `backoff sleep only spans the remaining window when time has partially elapsed`() {
        server.enqueue(jsonResponse("""{"items":[],"backoff":10}"""))
        server.enqueue(jsonResponse("""{"items":[]}"""))

        execute()
        now += 4_000L
        execute()

        assertEquals(listOf(6_000L), sleeps)
    }

    @Test
    fun `non-json content type is ignored`() {
        server.enqueue(MockResponse().setBody("""{"items":[],"backoff":9}""").setHeader("Content-Type", "text/plain"))
        server.enqueue(jsonResponse("""{"items":[]}"""))

        execute()
        execute()

        assertEquals(emptyList<Long>(), sleeps)
    }

    @Test
    fun `error responses are not inspected for backoff`() {
        server.enqueue(jsonResponse("""{"backoff":4}""").setResponseCode(500))
        server.enqueue(jsonResponse("""{"items":[]}"""))

        execute()
        execute()

        assertEquals(emptyList<Long>(), sleeps)
    }

    private fun execute() {
        client.newCall(Request.Builder().url(server.url("/")).build()).execute().close()
    }

    private fun jsonResponse(body: String): MockResponse =
        MockResponse().setBody(body).setHeader("Content-Type", "application/json")
}
