package com.laarasoft.frontend.features.websocket.data.realtime

import com.laarasoft.frontend.features.websocket.domain.realtime.ConnectionState
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

interface WebsocketManager {
    val state: StateFlow<ConnectionState>
    val messages: SharedFlow<String>
    suspend fun connect()
    suspend fun disconnect()

    // no delivery gurantte in this, use app level acks for that
    fun send(text: String) : Boolean

    fun onNetworkAvailable()
}

data class WebSocketConfig(
    val url: String,
    val stableAfter: Duration = 10.seconds
)