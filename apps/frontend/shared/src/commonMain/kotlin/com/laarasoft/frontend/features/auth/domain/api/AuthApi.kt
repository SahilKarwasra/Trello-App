package com.laarasoft.frontend.features.auth.domain.api

import com.laarasoft.frontend.config.network.DataError
import com.laarasoft.frontend.config.network.Result
import com.laarasoft.frontend.features.auth.data.dto.AuthResponseDto
import com.laarasoft.frontend.features.auth.data.dto.SignInRequestDto
import com.laarasoft.frontend.features.auth.data.dto.SignUpRequestDto

interface AuthApi {
    suspend fun signIn(request: SignInRequestDto): Result<AuthResponseDto, DataError.Remote>
    suspend fun signUp(request: SignUpRequestDto): Result<AuthResponseDto, DataError.Remote>
}