package com.laarasoft.frontend.features.auth.presentation.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laarasoft.frontend.config.navigation.AuthScreenDestination
import com.laarasoft.frontend.config.network.onError
import com.laarasoft.frontend.config.network.onSuccess
import com.laarasoft.frontend.config.network.sendSnackbarOnError
import com.laarasoft.frontend.core.utils.SessionManager
import com.laarasoft.frontend.core.utils.ui.UiEvent
import com.laarasoft.frontend.core.utils.ui.UiEventController
import com.laarasoft.frontend.features.auth.domain.repository.AuthRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SignupViewModel(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {

    private val _state = MutableStateFlow(SignupState())
    val state = _state.asStateFlow()

    private val _events = Channel<SignupEvents>()
    val events = _events.receiveAsFlow()

    fun onAction(action: SignupAction) {
        when (action) {
            is SignupAction.OnUsernameChange -> {
                _state.update { it.copy(username = action.username, errorMessage = null) }
            }

            is SignupAction.OnPasswordChange -> {
                _state.update { it.copy(password = action.password, errorMessage = null) }
            }

            is SignupAction.OnTogglePasswordVisibility -> {
                _state.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
            }

            is SignupAction.OnSubmit -> {
                signup()
            }

            is SignupAction.OnNavigateToLogin -> {
                viewModelScope.launch {
                    _events.send(SignupEvents.NavigateToLogin(AuthScreenDestination.LoginScreen))
                }
            }
        }
    }

    private fun signup() {
        val username = _state.value.username.trim()
        val password = _state.value.password

        if (username.isBlank() || password.isBlank()) {
            val message = "Username and password cannot be empty"
            _state.update { it.copy(errorMessage = message) }
            viewModelScope.launch {
                UiEventController.send(UiEvent.Snackbar(message))
            }
            return
        }

        if (username.length < 3) {
            val message = "Username must be at least 3 characters"
            _state.update { it.copy(errorMessage = message) }
            viewModelScope.launch {
                UiEventController.send(UiEvent.Snackbar(message))
            }
            return
        }

        if (password.length < 6) {
            val message = "Password must be at least 6 characters"
            _state.update { it.copy(errorMessage = message) }
            viewModelScope.launch {
                UiEventController.send(UiEvent.Snackbar(message))
            }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            authRepository.signup(username, password)
                .onSuccess {
                    _state.update { it.copy(isLoading = false) }
                    sessionManager.resetExpirationState()
                    UiEventController.send(UiEvent.Snackbar("User registered successfully"))
                    _events.send(SignupEvents.NavigateToOrganisations())
                }.onError { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Failed to sign up"
                        )
                    }
                }.sendSnackbarOnError(skipAuth = true)

        }
    }
}
