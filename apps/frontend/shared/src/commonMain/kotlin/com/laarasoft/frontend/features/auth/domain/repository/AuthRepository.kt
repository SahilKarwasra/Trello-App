package com.laarasoft.frontend.features.auth.domain.repository

import com.laarasoft.frontend.config.network.DataError
import com.laarasoft.frontend.config.network.Result
import com.laarasoft.frontend.features.auth.domain.model.AuthData

interface AuthRepository {
    suspend fun login(username: String, password: String): Result<AuthData, DataError.Remote>
    suspend fun signup(username: String, password: String): Result<AuthData, DataError.Remote>
}