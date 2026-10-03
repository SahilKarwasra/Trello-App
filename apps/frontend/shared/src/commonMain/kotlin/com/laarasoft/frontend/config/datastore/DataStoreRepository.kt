package com.laarasoft.frontend.config.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.laarasoft.frontend.config.datastore.DataStoreKeys.ACCESS_TOKEN
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class DataStoreRepository (
    private val dataStore: DataStore<Preferences>
) {
    suspend fun getToken(): String? = authToken.first()

    suspend fun saveToken(token: String) {
        dataStore.edit { it[ACCESS_TOKEN] = token }
    }

    val authToken: Flow<String?> = dataStore.data.map { it[ACCESS_TOKEN] }

    suspend fun clearTokens() {
        withContext(Dispatchers.IO) {
            dataStore.edit { preferences ->
                preferences.remove(ACCESS_TOKEN)
            }
        }
    }

    suspend fun clearAll() {
        withContext(Dispatchers.IO) {
            dataStore.edit { preferences ->
                preferences.clear()
            }
        }
    }
}