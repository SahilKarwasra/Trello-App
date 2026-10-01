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
    data object CreateOrSelectOrganisationScreen: AuthScreenDestination
    @Serializable
    data object SplashScreen: AuthScreenDestination
}

sealed interface UserScreenDestination {
    @Serializable
    data class HomeScreen(
        val orgId: String = "",
        val orgName: String = ""
    ) : UserScreenDestination
    @Serializable
    data object KanbanScreen : UserScreenDestination
}