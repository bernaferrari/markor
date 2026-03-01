package com.bernaferrari.remarkor.ui.components

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Sealed class representing different types of user messages.
 */
sealed class UserMessage {
    data class Error(val message: String, val action: (() -> Unit)? = null) : UserMessage()
    data class Success(val message: String) : UserMessage()
    data class Info(
        val message: String,
        val actionLabel: String? = null,
        val action: (() -> Unit)? = null
    ) : UserMessage()

    data class Confirmation(
        val message: String,
        val confirmLabel: String = "Confirm",
        val dismissLabel: String = "Cancel",
        val onConfirm: () -> Unit,
        val onDismiss: () -> Unit = {}
    ) : UserMessage()
}

/**
 * Manages user messages using a Flow for reactive UI updates.
 */
class UserMessageManager {
    private val _messages = MutableSharedFlow<UserMessage>(extraBufferCapacity = 16)
    val messages: Flow<UserMessage> = _messages.asSharedFlow()

    fun showMessage(message: UserMessage) {
        _messages.tryEmit(message)
    }

    fun error(message: String, action: (() -> Unit)? = null) {
        showMessage(UserMessage.Error(message, action))
    }

    fun success(message: String) {
        showMessage(UserMessage.Success(message))
    }

    fun info(message: String, actionLabel: String? = null, action: (() -> Unit)? = null) {
        showMessage(UserMessage.Info(message, actionLabel, action))
    }

    fun confirm(
        message: String,
        confirmLabel: String = "Confirm",
        dismissLabel: String = "Cancel",
        onConfirm: () -> Unit,
        onDismiss: () -> Unit = {}
    ) {
        showMessage(
            UserMessage.Confirmation(
                message,
                confirmLabel,
                dismissLabel,
                onConfirm,
                onDismiss
            )
        )
    }
}

/**
 * Composable that handles displaying user messages via Snackbar.
 */
@Composable
fun rememberUserMessageManager(): UserMessageManager {
    return remember { UserMessageManager() }
}

/**
 * Composable that connects UserMessageManager to SnackbarHostState.
 */
@Composable
fun UserMessageHandler(
    messageManager: UserMessageManager,
    snackbarHostState: SnackbarHostState,
    scope: CoroutineScope = rememberCoroutineScope()
) {
    LaunchedEffect(messageManager) {
        messageManager.messages.collect { message ->
            when (message) {
                is UserMessage.Error -> {
                    val result = snackbarHostState.showSnackbar(
                        message = "Error: ${message.message}",
                        duration = SnackbarDuration.Long,
                        withDismissAction = true
                    )
                }

                is UserMessage.Success -> {
                    snackbarHostState.showSnackbar(
                        message = message.message,
                        duration = SnackbarDuration.Short
                    )
                }

                is UserMessage.Info -> {
                    val result = snackbarHostState.showSnackbar(
                        message = message.message,
                        actionLabel = message.actionLabel,
                        duration = SnackbarDuration.Short
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        message.action?.invoke()
                    }
                }

                is UserMessage.Confirmation -> {
                    val result = snackbarHostState.showSnackbar(
                        message = message.message,
                        actionLabel = message.confirmLabel,
                        duration = SnackbarDuration.Indefinite,
                        withDismissAction = true
                    )
                    when (result) {
                        SnackbarResult.ActionPerformed -> message.onConfirm()
                        SnackbarResult.Dismissed -> message.onDismiss()
                    }
                }
            }
        }
    }
}

/**
 * Result wrapper for operations that can fail.
 */
sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val message: String, val exception: Throwable? = null) : Result<Nothing>()

    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error

    fun getOrNull(): T? = (this as? Success)?.data
    fun getErrorMessage(): String? = (this as? Error)?.message

    inline fun <R> map(transform: (T) -> R): Result<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> this
    }

    inline fun onSuccess(action: (T) -> Unit): Result<T> {
        if (this is Success) action(data)
        return this
    }

    inline fun onError(action: (String) -> Unit): Result<T> {
        if (this is Error) action(message)
        return this
    }

    companion object {
        inline fun <T> catching(block: () -> T): Result<T> = try {
            Success(block())
        } catch (e: Exception) {
            Error(e.message ?: "Unknown error", e)
        }
    }
}

/**
 * Extension to handle Result with UserMessageManager.
 */
fun <T> Result<T>.handleWith(
    messageManager: UserMessageManager,
    onSuccessMessage: String? = null,
    onErrorPrefix: String = ""
): Result<T> {
    if (this is Result.Error) {
        messageManager.error(if (onErrorPrefix.isNotEmpty()) "$onErrorPrefix: $message" else message)
    } else if (this is Result.Success && onSuccessMessage != null) {
        messageManager.success(onSuccessMessage)
    }
    return this
}
