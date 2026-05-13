package com.fnabl.data.remote

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Honours the Stack Exchange API's `backoff` field. When a response carries `"backoff": N`, the
 * client must wait N seconds before hitting any endpoint with a similar path or the API replies
 * with `throttle_violation` on the next call.
 */
internal class BackoffInterceptor(
    private val json: Json,
    private val clock: () -> Long = System::currentTimeMillis,
    private val sleeper: (Long) -> Unit = Thread::sleep,
) : Interceptor {
    @Volatile private var nextAllowedAt: Long = 0L
    private val lock = Any()

    override fun intercept(chain: Interceptor.Chain): Response {
        sleepUntilClear()
        val response = chain.proceed(chain.request())
        recordBackoffIfPresent(response)
        return response
    }

    private fun sleepUntilClear() {
        val wait = nextAllowedAt - clock()
        if (wait > 0) sleeper(wait)
    }

    private fun recordBackoffIfPresent(response: Response) {
        if (!response.isSuccessful) return
        val contentType = response.header("Content-Type") ?: return
        if (!contentType.contains("json", ignoreCase = true)) return

        val body = response.peekBody(MAX_PEEK_BYTES).string()
        val seconds =
            runCatching {
                json.parseToJsonElement(body).jsonObject["backoff"]?.jsonPrimitive?.intOrNull
            }.getOrNull() ?: return
        if (seconds <= 0) return

        synchronized(lock) {
            val candidate = clock() + seconds * 1000L
            if (candidate > nextAllowedAt) nextAllowedAt = candidate
        }
    }

    private companion object {
        const val MAX_PEEK_BYTES = 1L * 1024 * 1024
    }
}
