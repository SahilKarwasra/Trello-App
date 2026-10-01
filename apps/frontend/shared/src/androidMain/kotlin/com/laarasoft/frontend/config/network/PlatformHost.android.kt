package com.laarasoft.frontend.config.network

/**
 * On Android, we use "localhost" with `adb reverse tcp:8080 tcp:8080`
 * which forwards the emulator's localhost:8080 to the host machine's localhost:8080.
 *
 * This is more reliable than 10.0.2.2 which can fail when the backend
 * binds to IPv6 only.
 *
 * Run this in terminal before launching the app:
 *   adb reverse tcp:8080 tcp:8080
 */
actual val PLATFORM_HOST: String = "10.0.2.2"
