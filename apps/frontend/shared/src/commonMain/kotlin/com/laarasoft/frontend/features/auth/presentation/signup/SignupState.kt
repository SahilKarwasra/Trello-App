package com.laarasoft.frontend.features.auth.presentation.signup

data class SignupState(
    val username: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
