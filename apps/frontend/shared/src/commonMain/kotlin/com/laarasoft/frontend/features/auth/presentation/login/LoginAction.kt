package com.laarasoft.frontend.features.auth.presentation.login

sealed interface LoginAction {
    data class OnUsernameChange(val username: String) : LoginAction
    data class OnPasswordChange(val password: String) : LoginAction
    data object OnTogglePasswordVisibility : LoginAction
    data object OnSubmit : LoginAction
    data object OnNavigateToSignup : LoginAction
}