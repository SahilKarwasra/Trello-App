package com.laarasoft.frontend.features.auth.data.dto

import com.laarasoft.frontend.features.auth.domain.model.AuthData
import com.laarasoft.frontend.features.auth.domain.model.User
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SignInRequestDto(
    @SerialName("username")
    val username: String,
    @SerialName("password")
    val password: String
)

@Serializable
data class SignUpRequestDto(
    @SerialName("username")
    val username: String,
    @SerialName("password")
    val password: String
)

@Serializable
data class AuthResponseDto(
    val token: String,
    val user: UserDto
)

@Serializable
data class UserDto(
    val id: String,
    val username: String,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null
)

fun UserDto.toDomain(): User = User(
    id = id,
    username = username,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun AuthResponseDto.toDomain(): AuthData = AuthData(
    token = token,
    user = user.toDomain()
)
