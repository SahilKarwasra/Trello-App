package com.laarasoft.frontend.features.websocket.domain.realtime

import kotlin.time.Duration

sealed interface ConnectionState {
    data object Disconnected: ConnectionState
    data object Connecting: ConnectionState
    data object Connected: ConnectionState
    data class Reconnecting(val attempts: Int, val retryIn: Duration): ConnectionState
    data class Failed(val reason: FailureReason): ConnectionState
}

enum class FailureReason {
    UNAUTHORIZED,
    POLICY_VIOLATION
}