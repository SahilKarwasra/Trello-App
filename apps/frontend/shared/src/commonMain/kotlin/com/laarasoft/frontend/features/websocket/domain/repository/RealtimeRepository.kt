package com.laarasoft.frontend.features.websocket.domain.repository

import com.laarasoft.frontend.features.websocket.domain.models.BoardEvent
import com.laarasoft.frontend.features.websocket.domain.realtime.ConnectionState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface RealtimeRepository {
    val connectionState: StateFlow<ConnectionState>
    fun observeBoard(boardId: String): Flow<BoardEvent>
    fun notifyNetworkAvailable()
    suspend fun disconnect()
    fun disconnectAsync()
}