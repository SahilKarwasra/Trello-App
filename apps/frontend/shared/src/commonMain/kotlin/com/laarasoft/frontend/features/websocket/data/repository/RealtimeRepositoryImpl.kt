package com.laarasoft.frontend.features.websocket.data.repository

import com.laarasoft.frontend.features.websocket.data.dto.BoardEventMapper
import com.laarasoft.frontend.features.websocket.data.dto.WsEnvelope
import com.laarasoft.frontend.features.websocket.data.realtime.WebsocketManager
import com.laarasoft.frontend.features.websocket.domain.models.BoardEvent
import com.laarasoft.frontend.features.websocket.domain.realtime.ConnectionState
import com.laarasoft.frontend.features.websocket.domain.repository.RealtimeRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class RealtimeRepositoryImpl internal constructor(
    private val ws: WebsocketManager,
    private val json: Json,
    private val scope: CoroutineScope,
    private val idleDisconnectDelay: Duration = 5.seconds,
) : RealtimeRepository {

    private val mapper = BoardEventMapper(json)
    private val mutex = Mutex()
    private val refCounts = mutableMapOf<String, Int>()
    private var idleJob: Job? = null
    private var hasConnectedBefore = false

    private val resync = MutableSharedFlow<BoardEvent>(
        extraBufferCapacity = 16,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    override val connectionState: StateFlow<ConnectionState> = ws.state

    init {
        // On reconnect, emit ResyncRequired for all active boards so the UI can refresh
        scope.launch {
            ws.state.filterIsInstance<ConnectionState.Connected>().collect {
                val (boards, isReconnect) = mutex.withLock {
                    val snapshot = refCounts.keys.toList()
                    val reconnect = hasConnectedBefore
                    hasConnectedBefore = true
                    snapshot to reconnect
                }
                if (isReconnect) boards.forEach { resync.tryEmit(BoardEvent.ResyncRequired(it)) }
            }
        }
    }

    override fun observeBoard(boardId: String): Flow<BoardEvent> = channelFlow {
        launch(start = CoroutineStart.UNDISPATCHED) {
            ws.messages
                .mapNotNull(::decode)
                .filter { it.boardId == boardId }
                .collect { send(it) }
        }
        launch(start = CoroutineStart.UNDISPATCHED) {
            resync.filter { it.boardId == boardId }.collect { send(it) }
        }

        acquire(boardId)
        try {
            awaitCancellation()
        } finally {
            withContext(NonCancellable) { release(boardId) }
        }
    }

    override fun notifyNetworkAvailable() = ws.onNetworkAvailable()


    private fun decode(text: String): BoardEvent? =
        runCatching { mapper.map(json.decodeFromString(WsEnvelope.serializer(), text)) }.getOrNull()

    private suspend fun acquire(boardId: String) {
        mutex.withLock {
            idleJob?.cancel()
            idleJob = null
            val count = (refCounts[boardId] ?: 0) + 1
            refCounts[boardId] = count
        }
        ws.connect()
    }

    private suspend fun release(boardId: String) = mutex.withLock {
        val count = (refCounts[boardId] ?: return@withLock) - 1
        if (count <= 0) {
            refCounts.remove(boardId)
        } else {
            refCounts[boardId] = count
        }
        if (refCounts.isEmpty()) scheduleIdleDisconnect()
    }

    override suspend fun disconnect() {
        mutex.withLock {
            idleJob?.cancel()
            idleJob = null
            refCounts.clear()
            hasConnectedBefore = false
        }
        ws.disconnect()
    }

    private fun scheduleIdleDisconnect() {
        idleJob?.cancel()
        idleJob = scope.launch {
            delay(idleDisconnectDelay)
            mutex.withLock {
                if (refCounts.isEmpty()) {
                    ws.disconnect()
                    hasConnectedBefore = false
                }
            }
        }
    }
}