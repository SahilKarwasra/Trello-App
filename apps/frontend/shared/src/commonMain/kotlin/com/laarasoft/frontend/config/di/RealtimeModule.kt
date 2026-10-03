package com.laarasoft.frontend.config.di

import com.laarasoft.frontend.features.websocket.data.realtime.KtorWebSocketManager
import com.laarasoft.frontend.features.websocket.data.realtime.WebSocketConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.dsl.singleOf
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
    singleOf(::WebSocketConfig)

    singleOf(::KtorWebSocketManager)

//    singleOf(:: RealtimeRepositoryImpl)
//        .bind<RealtimeRepository>()
//
//    singleOf(::ObserveBoardEventsUseCase)
//
//    singleOf(::ObserveConnectionStateUseCase)
}