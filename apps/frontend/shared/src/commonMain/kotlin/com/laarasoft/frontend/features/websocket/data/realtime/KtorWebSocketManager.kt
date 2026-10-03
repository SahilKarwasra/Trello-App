package com.laarasoft.frontend.features.websocket.data.realtime

import com.laarasoft.frontend.features.websocket.domain.realtime.ConnectionState
import com.laarasoft.frontend.features.websocket.domain.realtime.FailureReason
import io.ktor.client.HttpClient
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.client.request.bearerAuth
import io.ktor.http.HttpStatusCode
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readReason
import io.ktor.websocket.readText
import io.ktor.websocket.send
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource

class KtorWebSocketManager(
    private val httpClient: HttpClient,
    private val config: WebSocketConfig,
    private val tokenProvider: suspend () -> String,
    private val scope: CoroutineScope,
    private val log: (message: String, cause: Throwable?) -> Unit = { _, _ -> },
    private val backoff: BackoffPolicy = ExponentialBackoff(),
) : WebsocketManager {

    private val _state = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    override val state = _state.asStateFlow()
    private val _messages = MutableSharedFlow<String>(
        extraBufferCapacity = 256,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    override val messages: SharedFlow<String> = _messages.asSharedFlow()

    private val outbox = Channel<String>(capacity = 64)
    private val wakeUp = Channel<Unit>(Channel.CONFLATED)
    private val lifecycleLock = Mutex()
    private var connectionJob: Job? = null
    private var activeBoardId: String? = null

    override suspend fun connect(boardId: String?) = lifecycleLock.withLock {
        if (boardId != null && boardId != activeBoardId) {
            activeBoardId = boardId
            connectionJob?.cancelAndJoin()
            connectionJob = null
        } else if (boardId != null) {
            activeBoardId = boardId
        }

        if (connectionJob?.isActive == true) return@withLock
        connectionJob = scope.launch { runLoop() }
    }

    override suspend fun disconnect() = lifecycleLock.withLock {
        connectionJob?.cancelAndJoin()
        connectionJob = null
        activeBoardId = null
        _state.value = ConnectionState.Disconnected
    }

    override fun send(text: String): Boolean =
        _state.value is ConnectionState.Connected && outbox.trySend(text).isSuccess

    override fun onNetworkAvailable() {
        if (_state.value is ConnectionState.Reconnecting) {
            wakeUp.trySend(Unit)
        }
    }

    private suspend fun runLoop() {
        var attempt = 0
        while (true) {
            if (attempt == 0) _state.value = ConnectionState.Connecting

            var connectedAt: TimeMark? = null
            val terminal: ConnectionState.Failed? = try {
                val close = runSession { connectedAt = TimeSource.Monotonic.markNow() }
                if (close?.code == CloseReason.Codes.VIOLATED_POLICY.code) {
                    ConnectionState.Failed(FailureReason.POLICY_VIOLATION)
                } else null
            } catch (e: CancellationException) {
                throw e
            } catch (e: ResponseException) {
                val s = e.response.status
                if (s == HttpStatusCode.Unauthorized || s == HttpStatusCode.Forbidden) {
                    ConnectionState.Failed(FailureReason.UNAUTHORIZED)
                } else null
            } catch (e: Throwable) {
                null
            }

            if (terminal != null) {
                _state.value = terminal
                return
            }

            if (connectedAt?.elapsedNow()?.let { it >= config.stableAfter } == true) attempt = 0
            attempt++

            val wait = backoff.delayFor(attempt)
            _state.value = ConnectionState.Reconnecting(attempt, wait)
            withTimeoutOrNull(wait) { wakeUp.receive() }
        }
    }

    private suspend fun runSession(onOpen: () -> Unit): CloseReason? {
        val token = tokenProvider()
        val boardId = activeBoardId

        val fullUrl = buildString {
            append(config.url)
            val hasQuery = config.url.contains("?")
            var first = !hasQuery
            fun addParam(k: String, v: String) {
                if (first) {
                    append("?")
                    first = false
                } else {
                    append("&")
                }
                append(k).append("=").append(v)
            }
            if (!boardId.isNullOrBlank()) {
                addParam("board_id", boardId)
            }
            if (token.isNotBlank()) {
                addParam("token", token)
            }
        }

        val session = httpClient.webSocketSession(fullUrl) {
            if (token.isNotBlank()) {
                bearerAuth(token)
            }
        }

        try {
            while (outbox.tryReceive().isSuccess) { /* drop stale messages */ }
            _state.value = ConnectionState.Connected
            onOpen()

            coroutineScope {
                val writer = launch {
                    for (text in outbox) {
                        session.send(text)
                    }
                }
                try {
                    for (frame in session.incoming) {
                        if (frame is Frame.Text) {
                            val text = frame.readText()
                            text.lines().filter { it.isNotBlank() }.forEach { line ->
                                _messages.tryEmit(line)
                            }
                        }
                    }
                } finally {
                    writer.cancel()
                }
            }
            return withTimeoutOrNull(1.seconds) { session.closeReason.await() }
        } finally {
            withContext(NonCancellable) { runCatching { session.close() } }
        }
    }
}