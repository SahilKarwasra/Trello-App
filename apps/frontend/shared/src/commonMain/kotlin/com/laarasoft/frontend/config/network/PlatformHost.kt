package com.laarasoft.frontend.config.network

/**
 * Platform-specific default host for connecting to local development backend.
 * - Android Emulator: 10.0.2.2 (or 10.0.3.2 for Genymotion)
 * - Desktop / iOS Simulator: localhost
 */
expect val PLATFORM_HOST: String
