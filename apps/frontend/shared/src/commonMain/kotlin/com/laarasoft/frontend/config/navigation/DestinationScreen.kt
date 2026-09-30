package com.laarasoft.frontend.config.navigation

import kotlinx.serialization.Serializable

sealed interface MainGraph {
    @Serializable
    data object HomeGraph: MainGraph
    @Serializable
    data object AuthGraph: MainGraph
}

sealed interface AuthScreenDestination {
    @Serializable
    data object LoginScreen: AuthScreenDestination
    @Serializable
    data object SignupScreen: AuthScreenDestination
    @Serializable
    data object CreateOrganisationScreen: AuthScreenDestination
    @Serializable
    data object SplashScreen: AuthScreenDestination
}

sealed interface UserScreenDestination {
    @Serializable
    data object HomeScreen : UserScreenDestination
    @Serializable
    data object KanbanScreen : UserScreenDestination
}