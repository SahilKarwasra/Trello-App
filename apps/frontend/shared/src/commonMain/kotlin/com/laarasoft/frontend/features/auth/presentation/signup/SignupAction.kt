package com.laarasoft.frontend.features.auth.presentation.signup

sealed interface SignupAction {
    data class OnUsernameChange(val username: String) : SignupAction
    data class OnPasswordChange(val password: String) : SignupAction
    data object OnTogglePasswordVisibility : SignupAction
    data object OnSubmit : SignupAction
    data object OnNavigateToLogin : SignupAction
}
