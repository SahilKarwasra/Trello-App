package com.laarasoft.frontend.features.auth.presentation.signup

import com.laarasoft.frontend.config.navigation.AuthScreenDestination

sealed interface SignupEvents {
    data class NavigateToLogin(val destination: AuthScreenDestination = AuthScreenDestination.LoginScreen) : SignupEvents
    data class NavigateToOrganisations(val destination: AuthScreenDestination = AuthScreenDestination.CreateOrSelectOrganisationScreen) : SignupEvents
}
