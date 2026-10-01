package com.laarasoft.frontend.features.splash.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.laarasoft.frontend.config.navigation.AuthScreenDestination
import com.laarasoft.frontend.config.navigation.MainGraph
import com.laarasoft.frontend.core.theme.TrelloTheme
import com.laarasoft.frontend.core.utils.ObserveAsEvents
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SplashRoot(
    viewModel: SplashViewModel = koinViewModel(),
    navigateToAuth: (AuthScreenDestination) -> Unit,
    navigateToOrganisations: (AuthScreenDestination) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is SplashEvents.NavigateToAuth -> navigateToAuth(event.destination)
            is SplashEvents.NavigateToOrganisations -> navigateToOrganisations(event.destination)
            is SplashEvents.NavigateToHome -> { /* unused — reserved for future direct-home routing */ }
        }
    }

    SplashScreen(state = state, onAction = viewModel::onAction)
}

@Composable
fun SplashScreen(
    state: SplashState,
    onAction: (SplashAction) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Trello",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(24.dp))
            CircularProgressIndicator(
                modifier = Modifier.size(32.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 3.dp
            )
        }
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