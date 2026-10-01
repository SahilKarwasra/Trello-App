package com.laarasoft.frontend.features.auth.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laarasoft.frontend.config.navigation.AuthScreenDestination
import com.laarasoft.frontend.config.network.onError
import com.laarasoft.frontend.config.network.onSuccess
import com.laarasoft.frontend.config.network.sendSnackbarOnError
import com.laarasoft.frontend.core.utils.ui.UiEvent
import com.laarasoft.frontend.core.utils.ui.UiEventController
import com.laarasoft.frontend.features.auth.domain.repository.AuthRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state = _state.asStateFlow()

    private val _events = Channel<LoginEvents>()
    val events = _events.receiveAsFlow()

    fun onAction(action: LoginAction) {
        when (action) {
            is LoginAction.OnUsernameChange -> {
                _state.update { it.copy(username = action.username, errorMessage = null) }
            }

            is LoginAction.OnPasswordChange -> {
                _state.update { it.copy(password = action.password, errorMessage = null) }
            }

            is LoginAction.OnTogglePasswordVisibility -> {
                _state.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
            }

            is LoginAction.OnSubmit -> {
                login()
            }

            is LoginAction.OnNavigateToSignup -> {
                viewModelScope.launch {
                    _events.send(LoginEvents.NavigateToSignup(AuthScreenDestination.SignupScreen))
                }
            }
        }
    }

    private fun login() {
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

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            authRepository.login(username, password)
                .onSuccess {
                    _state.update { it.copy(isLoading = false) }
                    UiEventController.send(UiEvent.Snackbar("Signed in successfully"))
                    _events.send(LoginEvents.NavigateToOrganisations())
                }.onError { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Invalid username or password"
                        )
                    }
                }
                .sendSnackbarOnError(skipAuth = true)
        }
    }
}