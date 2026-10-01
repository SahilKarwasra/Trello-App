package com.laarasoft.frontend.features.auth.domain.model

data class User(
    val id: String,
    val username: String,
    val createdAt: String? = null,
    val updatedAt: String? = null
)
