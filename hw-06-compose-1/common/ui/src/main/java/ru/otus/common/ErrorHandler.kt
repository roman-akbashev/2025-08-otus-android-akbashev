package ru.otus.common

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun ErrorHandler(
    hasError: Boolean,
    errorMessage: String,
    onErrorShown: () -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    snackbarDuration: SnackbarDuration = SnackbarDuration.Short,
    content: @Composable () -> Unit
) {
    Box(modifier) {
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.TopCenter)
        )

        LaunchedEffect(hasError, errorMessage) {
            if (hasError) {
                snackbarHostState.showSnackbar(
                    message = errorMessage,
                    duration = snackbarDuration
                )
                onErrorShown()
            }
        }

        content()
    }
}