package com.laarasoft.frontend.core.utils

import com.laarasoft.frontend.config.datastore.DataStoreRepository

class TokenProvider(
    private val dataStoreRepository: DataStoreRepository? = null,
) {
    private var inMemoryToken: String? = null

    suspend fun getAccessToken(): String? =
        dataStoreRepository?.getToken() ?: inMemoryToken

    suspend fun updateAccessToken(token: String) {
        inMemoryToken = token
        dataStoreRepository?.saveToken(token)
    }

    suspend fun clearToken() {
        inMemoryToken = null
        dataStoreRepository?.clearAll()
    }
}