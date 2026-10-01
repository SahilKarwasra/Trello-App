package com.laarasoft.frontend.config.network

import android.util.Log
import io.ktor.client.plugins.logging.Logger

actual val platformHttpLogger: Logger = object : Logger {
    private val TAG = "KTOR_API"

    override fun log(message: String) {
        if (message.length > 3000) {
            message.chunked(3000).forEach { chunk ->
                Log.d(TAG, chunk)
            }
        } else {
            Log.d(TAG, message)
        }
    }
}
