package com.laarasoft.frontend.core.utils

import com.laarasoft.frontend.config.datastore.DataStoreRepository

class TokenProvider(
    private val dataStoreRepository: DataStoreRepository,
) {
    suspend fun getAccessToken(): String? =
        dataStoreRepository.getToken()

    suspend fun updateAccessToken(token: String) {
        dataStoreRepository.saveToken(token)
    }

    suspend fun clearToken() {
        dataStoreRepository.clearTokens()
    }
}