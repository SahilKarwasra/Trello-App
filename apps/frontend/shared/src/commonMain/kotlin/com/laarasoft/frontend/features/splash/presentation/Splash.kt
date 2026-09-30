package com.laarasoft.frontend.features.splash.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.laarasoft.frontend.config.navigation.AuthScreenDestination
import com.laarasoft.frontend.config.navigation.UserScreenDestination
import com.laarasoft.frontend.core.theme.TrelloTheme
import com.laarasoft.frontend.core.utils.ObserveAsEvents
import kotlinx.coroutines.delay
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun SplashRoot(
    viewModel: SplashViewModel = koinViewModel(),
    navigateToAuth: (AuthScreenDestination) -> Unit,
    navigateToHome: (UserScreenDestination) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ObserveAsEvents(viewModel.events) {
        when (it) {
            is SplashEvents.NavigateToAuth -> {
                navigateToAuth(it.destination)
            }

            is SplashEvents.NavigateToHome -> {
                navigateToHome(it.destination)
            }
        }
    }
    SplashScreen(
        state = state,
        onAction = viewModel::onAction
    )
}

@Composable
fun SplashScreen(
    state: SplashState,
    onAction: (SplashAction) -> Unit,
) {
    LaunchedEffect(Unit) {
        delay(2000.milliseconds)
        onAction(SplashAction.Init)
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            "Splash Screen",
            style = TextStyle(
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = MaterialTheme.typography.headlineLarge.fontSize
            )
        )
    }
}

@Preview
@Composable
private fun Preview() {
    TrelloTheme {
        SplashScreen(
            state = SplashState(),
            onAction = {}
        )
    }
}