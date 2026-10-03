package com.laarasoft.frontend.core.utils

import com.laarasoft.frontend.config.datastore.DataStoreRepository
import com.laarasoft.frontend.core.utils.ui.UiEvent
import com.laarasoft.frontend.core.utils.ui.UiEventController
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class SessionManager(
    private val tokenProvider: TokenProvider,
    private val dataStoreRepository: DataStoreRepository,
) {
    private val mutex = Mutex()
    private var isHandlingExpiration = false
    private var onSessionExpiredListener: (suspend () -> Unit)? = null

    fun setOnSessionExpiredListener(listener: suspend () -> Unit) {
        onSessionExpiredListener = listener
    }

    suspend fun handleSessionExpired() {
        mutex.withLock {
            // Clear token in memory and all preferences in datastore
            tokenProvider.clearToken()
            dataStoreRepository.clearAll()

            try {
                onSessionExpiredListener?.invoke()
            } catch (_: Exception) {}

            if (!isHandlingExpiration) {
                isHandlingExpiration = true
                UiEventController.send(UiEvent.SessionExpired)
            }
        }
    }

    fun resetExpirationState() {
        isHandlingExpiration = false
    }
}
