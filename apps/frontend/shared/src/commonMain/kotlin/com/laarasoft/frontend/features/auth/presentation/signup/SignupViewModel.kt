package com.laarasoft.frontend.features.auth.presentation.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SignupViewModel : ViewModel() {

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
                // UI only as requested (no backend integration yet)
            }
            is SignupAction.OnNavigateToLogin -> {
                viewModelScope.launch {
                    _events.send(SignupEvents.NavigateToLogin)
                }
            }
        }
    }
}
