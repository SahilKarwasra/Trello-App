package com.laarasoft.frontend.config.network

import io.ktor.client.plugins.logging.Logger

actual val platformHttpLogger: Logger = object : Logger {
    override fun log(message: String) {
        println("[KTOR_API] $message")
    }
}
