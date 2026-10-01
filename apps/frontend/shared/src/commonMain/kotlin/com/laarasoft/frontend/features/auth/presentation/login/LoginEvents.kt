package com.laarasoft.frontend.features.auth.presentation.login

import com.laarasoft.frontend.config.navigation.AuthScreenDestination
import com.laarasoft.frontend.config.navigation.MainGraph

sealed interface LoginEvents {
    data class NavigateToSignup(val destination: AuthScreenDestination = AuthScreenDestination.SignupScreen) : LoginEvents
    data class NavigateToOrganisations(val destination: AuthScreenDestination = AuthScreenDestination.CreateOrSelectOrganisationScreen) : LoginEvents
}