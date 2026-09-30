package com.laarasoft.frontend.features.auth.presentation.signup

sealed interface SignupEvents {
    data object NavigateToLogin : SignupEvents
    data object NavigateToHome : SignupEvents
}
