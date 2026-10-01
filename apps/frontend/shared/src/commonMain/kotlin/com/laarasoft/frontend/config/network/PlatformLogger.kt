package com.laarasoft.frontend.config.network

import io.ktor.client.plugins.logging.Logger

/**
 * Platform-specific HTTP logger for Ktor network requests and responses.
 * - Android: Logs to Android Logcat via android.util.Log.d with chunking to prevent truncation
 * - Desktop (JVM) & iOS (Native): Logs to console output (println)
 */
expect val platformHttpLogger: Logger
