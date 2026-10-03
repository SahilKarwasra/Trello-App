package com.laarasoft.frontend.features.websocket.data.realtime

import kotlin.math.pow
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

fun interface BackoffPolicy {
    fun delayFor(attempt: Int): Duration
}

// Exponential backoff with jitter (50-100% of the computed delay) to avoid thundering herds.
class ExponentialBackoff(
    private val base: Duration = 1.seconds,
    private val max: Duration = 30.seconds,
    private val random: Random = Random.Default,
) : BackoffPolicy {
    override fun delayFor(attempt: Int): Duration {
        val exp = base * 2.0.pow((attempt - 1).coerceIn(0, 16))
        val capped = minOf(exp, max)
        val jittered = capped.inWholeMilliseconds * (0.5 + random.nextDouble() * 0.5)
        return jittered.toLong().milliseconds
    }
}
