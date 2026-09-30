package com.laarasoft.frontend.core.utils.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun AppDialog(
    event: UiEvent.Dialog,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = {
            if (event.cancelable) {
                event.onDismiss?.invoke()
                onDismiss()
            }
        }
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 8.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(Modifier.height(12.dp))
                event.message()
                Spacer(Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    event.dismissText?.let {
                        TextButton(onClick = {
                            event.onDismiss?.invoke()
                            onDismiss()
                        }) {
                            Text(it)
                        }
                    }
                    Button(
                        onClick = {
                            event.onConfirm()
                            onDismiss()
                        },
                        colors = dialogButtonColors(event.buttonStyle)
                    ) {
                        Text(event.confirmText)
                    }
                }
            }
        }
    }
}

@Composable
fun dialogButtonColors(style: DialogButtonStyle): ButtonColors {
    return when (style) {
        DialogButtonStyle.Default -> ButtonDefaults.buttonColors()
        DialogButtonStyle.Primary -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
        DialogButtonStyle.Destructive -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onError
        )
    }
}