package com.laarasoft.frontend.features.splash.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laarasoft.frontend.core.utils.TokenProvider
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SplashViewModel(
    private val tokenProvider: TokenProvider
) : ViewModel() {

    private val _state = MutableStateFlow(SplashState())
    private val _events = Channel<SplashEvents>()
    val events = _events.receiveAsFlow()
    val state = _state
        .onStart { checkAuthState() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = SplashState()
        )

    private fun checkAuthState() {
        viewModelScope.launch {
            val token = tokenProvider.getAccessToken()
            if (token != null) {
                _events.send(SplashEvents.NavigateToOrganisations())
            } else {
                _events.send(SplashEvents.NavigateToAuth())
            }
        }
    }

    fun onAction(action: SplashAction) {
        // No actions needed — splash navigates automatically on start
    }
}