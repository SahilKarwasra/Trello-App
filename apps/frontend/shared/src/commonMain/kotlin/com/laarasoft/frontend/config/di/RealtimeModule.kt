package com.laarasoft.frontend.config.di

import com.laarasoft.frontend.core.utils.SessionManager
import com.laarasoft.frontend.core.utils.TokenProvider
import com.laarasoft.frontend.features.websocket.data.realtime.KtorWebSocketManager
import com.laarasoft.frontend.features.websocket.data.realtime.WebSocketConfig
import com.laarasoft.frontend.features.websocket.data.realtime.WebsocketManager
import com.laarasoft.frontend.features.websocket.data.repository.RealtimeRepositoryImpl
import com.laarasoft.frontend.features.websocket.domain.repository.RealtimeRepository
import com.laarasoft.frontend.features.websocket.domain.usecase.ObserveBoardEventsUseCase
import com.laarasoft.frontend.features.websocket.domain.usecase.ObserveConnectionStateUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val realTimeModule = module {
    single<CoroutineScope> {
        CoroutineScope(
            SupervisorJob() + Dispatchers.Default
        )
    }
    single {
        WebSocketConfig(
            url = "ws://localhost:8081/ws"
        )
    }

    single<WebsocketManager> {
        val tokenProvider: TokenProvider = get()
        KtorWebSocketManager(
            httpClient = get(),
            config = get(),
            tokenProvider = { tokenProvider.getAccessToken() ?: "" },
            scope = get()
        )
    }

    single<RealtimeRepository> {
        val repo = RealtimeRepositoryImpl(
            ws = get(),
            json = get(),
            scope = get(),
        )
        val sessionManager: SessionManager = get()
        sessionManager.setOnSessionExpiredListener {
            repo.disconnect()
        }
        repo
    }

    singleOf(::ObserveBoardEventsUseCase)

    singleOf(::ObserveConnectionStateUseCase)
}