package com.laarasoft.frontend.core.utils.ui

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.SnackbarDuration

object UiEventController {
    private val _events = Channel<UiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    suspend fun send(event: UiEvent) {
        _events.send(event)
    }

    fun trySend(event: UiEvent) {
        _events.trySend(event)
    }

}

sealed interface UiEvent {
    data class Snackbar(
        val message: String,
        val actionLabel: String? = null,
        val duration: SnackbarDuration = SnackbarDuration.Short,
        val withDismissAction: Boolean = false,
        val actionColor: Color? = null,
        val onAction: (() -> Unit)? = null
    ) : UiEvent
    data class Dialog(
        val title: String,
        val message: @Composable () -> Unit,
        val confirmText: String = "OK",
        val dismissText: String? = null,
        val buttonStyle: DialogButtonStyle = DialogButtonStyle.Default,
        val cancelable: Boolean = true,
        val onConfirm: () -> Unit,
        val onDismiss: (() -> Unit)? = null
    ) : UiEvent
    data class FullScreenDialog(
        val content: @Composable (dismiss: () -> Unit) -> Unit,
        val cancelable: Boolean = true,
        val onDismiss: (() -> Unit)? = null
    ) : UiEvent
    data object SessionExpired : UiEvent
}

enum class DialogButtonStyle {
    Default,
    Primary,
    Destructive
}