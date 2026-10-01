package com.laarasoft.frontend.config.navigation

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.toRoute
import com.laarasoft.frontend.core.utils.ObserveAsEvents
import com.laarasoft.frontend.core.utils.ui.DialogButtonStyle
import com.laarasoft.frontend.core.utils.ui.UiEvent
import com.laarasoft.frontend.core.utils.ui.UiEventController
import com.laarasoft.frontend.features.auth.presentation.login.LoginRoot
import com.laarasoft.frontend.features.auth.presentation.signup.SignupRoot
import com.laarasoft.frontend.features.organisation.presentation.CreateOrSelectOrgRoot
import com.laarasoft.frontend.features.home.presentation.HomeRoot
import com.laarasoft.frontend.features.splash.presentation.SplashRoot
import kotlinx.coroutines.launch

@Composable
fun AppNavigation() {
    val appController: TrelloController = rememberTrelloController()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var dialogEvent by remember { mutableStateOf<UiEvent.Dialog?>(null) }
    var fullScreenDialog by remember { mutableStateOf<UiEvent.FullScreenDialog?>(null) }

    ObserveAsEvents(UiEventController.events) { uiEvent ->
        when (uiEvent) {
            is UiEvent.Dialog -> {
                dialogEvent = uiEvent
            }

            is UiEvent.FullScreenDialog -> {
                fullScreenDialog = uiEvent
            }

            is UiEvent.Snackbar -> {
                scope.launch {
                    val result = snackbarHostState.showSnackbar(
                        message = uiEvent.message,
                        actionLabel = uiEvent.actionLabel,
                        withDismissAction = uiEvent.withDismissAction,
                        duration = uiEvent.duration
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        uiEvent.onAction?.invoke()
                    }
                }
            }

            is UiEvent.SessionExpired -> {
                dialogEvent = UiEvent.Dialog(
                    title = "Session Expired",
                    message = {
                        Text(text = "Please login again")
                    },
                    confirmText = "Ok",
                    buttonStyle = DialogButtonStyle.Primary,
                    cancelable = false,
                    onConfirm = {
                        appController.navigateToTop(MainGraph.AuthGraph)
                        dialogEvent = null
                    },
                    onDismiss = null
                )
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) {
                Snackbar(
                    snackbarData = it,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    actionColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    dismissActionContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    ) {
        NavHost(
            navController = appController.navController,
            startDestination = AuthScreenDestination.SplashScreen,
        ) {
            composable<AuthScreenDestination.SplashScreen> {
                SplashRoot(
                    navigateToAuth = {
                        appController.navigateToTop(MainGraph.AuthGraph)
                    },
                    navigateToOrganisations = {
                        appController.navigateToTop(AuthScreenDestination.CreateOrSelectOrganisationScreen)
                    }
                )
            }
            authGraph(appController)
            homeGraph(appController)
        }
    }
}

private fun NavGraphBuilder.authGraph(appController: TrelloController) {
    navigation<MainGraph.AuthGraph>(
        startDestination = AuthScreenDestination.LoginScreen
    ) {
        composable<AuthScreenDestination.LoginScreen> {
            LoginRoot(
                navigateToSignup =
                    appController::navigate,
                navigateToOrganisations =
                    appController::navigate
            )
        }
        composable<AuthScreenDestination.SignupScreen> {
            SignupRoot(
                navigateToLogin = {
                    appController.upPress()
                },
                navigateToOrganisations = appController::navigate
            )
        }
        composable<AuthScreenDestination.CreateOrSelectOrganisationScreen> {
            CreateOrSelectOrgRoot(
                navigateToHome = appController::navigateToTop
            )
        }
    }
}

private fun NavGraphBuilder.homeGraph(appController: TrelloController) {
    navigation<MainGraph.HomeGraph>(
        startDestination = UserScreenDestination.HomeScreen()
    ) {
        composable<UserScreenDestination.HomeScreen> { backStackEntry ->
            val homeScreen = backStackEntry.toRoute<UserScreenDestination.HomeScreen>()
            HomeRoot(
                orgId = homeScreen.orgId,
                orgName = homeScreen.orgName,
                navigateToChangeOrg = {
                    appController.navigateToTop(AuthScreenDestination.CreateOrSelectOrganisationScreen)
                },
                navigateToKanban = { boardId, boardTitle ->
                    // Reserved for future Kanban board navigation
                }
            )
        }
    }
}