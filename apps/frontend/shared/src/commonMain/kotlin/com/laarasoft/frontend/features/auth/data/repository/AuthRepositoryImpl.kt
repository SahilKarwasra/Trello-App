package com.laarasoft.frontend.features.auth.data.repository

import com.laarasoft.frontend.config.network.DataError
import com.laarasoft.frontend.config.network.Result
import com.laarasoft.frontend.config.network.map
import com.laarasoft.frontend.core.utils.TokenProvider
import com.laarasoft.frontend.features.auth.domain.api.AuthApi
import com.laarasoft.frontend.features.auth.data.dto.SignInRequestDto
import com.laarasoft.frontend.features.auth.data.dto.SignUpRequestDto
import com.laarasoft.frontend.features.auth.data.dto.toDomain
import com.laarasoft.frontend.features.auth.domain.model.AuthData
import com.laarasoft.frontend.features.auth.domain.repository.AuthRepository

class AuthRepositoryImpl(
    private val authApi: AuthApi,
    private val tokenProvider: TokenProvider
) : AuthRepository {

    override suspend fun login(username: String, password: String): Result<AuthData, DataError.Remote> {
        val result = authApi.signIn(
            SignInRequestDto(
                username = username,
                password = password
            )
        )

        return result.map { dto ->
            tokenProvider.updateAccessToken(dto.token)
            dto.toDomain()
        }
    }

    override suspend fun signup(username: String, password: String): Result<AuthData, DataError.Remote> {
        val result = authApi.signUp(
            SignUpRequestDto(
                username = username,
                password = password
            )
        )

        return result.map { dto ->
            tokenProvider.updateAccessToken(dto.token)
            dto.toDomain()
        }
    }
}
