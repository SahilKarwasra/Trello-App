package com.laarasoft.frontend.config.di

import org.koin.core.context.startKoin
import org.koin.core.error.KoinApplicationAlreadyStartedException
import org.koin.dsl.KoinAppDeclaration

fun initKoin(config: KoinAppDeclaration? = null) {
    try {
        startKoin {
            config?.invoke(this)
            modules(
                sharedModules,
                platformModule,
                realTimeModule
            )
        }
    } catch (_: KoinApplicationAlreadyStartedException) {
        // Koin is already running (e.g. Hot Reload re-composition). Safe to ignore.
    }
}