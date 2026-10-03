package com.laarasoft.frontend.features.websocket.domain.usecase

import com.laarasoft.frontend.features.websocket.domain.models.BoardEvent
import com.laarasoft.frontend.features.websocket.domain.realtime.ConnectionState
import com.laarasoft.frontend.features.websocket.domain.repository.RealtimeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

class ObserveBoardEventsUseCase(private val repository: RealtimeRepository) {
    operator fun invoke(boardId: String): Flow<BoardEvent> = repository.observeBoard(boardId)
}

class ObserveConnectionStateUseCase(private val repository: RealtimeRepository) {
    operator fun invoke(): StateFlow<ConnectionState> = repository.connectionState
}
