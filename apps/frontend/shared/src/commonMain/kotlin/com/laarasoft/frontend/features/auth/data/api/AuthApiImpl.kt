package com.laarasoft.frontend.features.auth.data.api

import com.laarasoft.frontend.config.network.DataError
import com.laarasoft.frontend.config.network.Endpoints
import com.laarasoft.frontend.config.network.Result
import com.laarasoft.frontend.config.network.safeCall
import com.laarasoft.frontend.features.auth.data.dto.AuthResponseDto
import com.laarasoft.frontend.features.auth.data.dto.SignInRequestDto
import com.laarasoft.frontend.features.auth.data.dto.SignUpRequestDto
import com.laarasoft.frontend.features.auth.domain.api.AuthApi
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class AuthApiImpl(
    private val httpClient: HttpClient
) : AuthApi {

    override suspend fun signIn(request: SignInRequestDto): Result<AuthResponseDto, DataError.Remote> {
        return safeCall<AuthResponseDto> {
            httpClient.post(Endpoints.SIGN_IN) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }
    }

    override suspend fun signUp(request: SignUpRequestDto): Result<AuthResponseDto, DataError.Remote> {
        return safeCall<AuthResponseDto> {
            httpClient.post(Endpoints.SIGN_UP) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }
    }
}
