package com.laarasoft.frontend.features.splash.presentation

import com.laarasoft.frontend.config.navigation.AuthScreenDestination
import com.laarasoft.frontend.config.navigation.UserScreenDestination

sealed interface SplashEvents {
    data class NavigateToAuth(val destination: AuthScreenDestination) : SplashEvents
    data class NavigateToHome(val destination: UserScreenDestination) : SplashEvents
}