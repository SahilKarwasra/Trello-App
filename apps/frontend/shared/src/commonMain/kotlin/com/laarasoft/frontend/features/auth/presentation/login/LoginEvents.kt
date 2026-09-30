package com.laarasoft.frontend.features.auth.presentation.login

import com.laarasoft.frontend.config.navigation.AuthScreenDestination
import com.laarasoft.frontend.config.navigation.UserScreenDestination

sealed interface LoginEvents {
    data class NavigateToSignup(val destination: AuthScreenDestination) : LoginEvents
    data class NavigateToHome(val destination: UserScreenDestination) : LoginEvents
}