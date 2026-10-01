package com.laarasoft.frontend.config.network

import com.laarasoft.frontend.core.utils.ui.UiEvent
import com.laarasoft.frontend.core.utils.ui.UiEventController

sealed interface Result<out D, out E : Error> {
    data class Success<out D>(val data: D) : Result<D, Nothing>
    data class Error<out E : com.laarasoft.frontend.config.network.Error>(val error: E) :
        Result<Nothing, E>
}

inline fun <T, E : Error, R> Result<T, E>.map(map: (T) -> R): Result<R, E> {
    return when (this) {
        is Result.Error -> Result.Error(error)
        is Result.Success -> Result.Success(map(data))
    }
}

fun <T, E : Error> Result<T, E>.asEmptyDataResult(): EmptyResult<E> {
    return map { }
}

inline fun <T, E : Error> Result<T, E>.onSuccess(action: (T) -> Unit): Result<T, E> {
    return when (this) {
        is Result.Error -> this
        is Result.Success -> {
            action(data)
            this
        }
    }
}

inline fun <T, E : Error> Result<T, E>.onError(action: (E) -> Unit): Result<T, E> {
    return when (this) {
        is Result.Error -> {
            action(error)
            this
        }

        is Result.Success -> this
    }
}

typealias EmptyResult<E> = Result<Unit, E>


suspend inline fun <T> Result<T, DataError.Local>.showLocalError(
    crossinline callback: (DataError) -> Unit = {}
): Result<T, DataError.Local> {
    return when (this) {
        is Result.Error -> {
            val msg = when (error.type) {
                DataError.Local.Type.UNKNOWN -> error.message ?: "Something went wrong"
                else -> {
                    null
                }
            }
            msg?.let {
                UiEventController.send(
                    UiEvent.Snackbar(message = msg, onAction = {
                        callback(error)
                    })
                )
            }

            this
        }

        is Result.Success -> this
    }
}


suspend inline fun <T> Result<T, DataError.Remote>.sendSnackbarOnError(
    skipAuth: Boolean = false,
    crossinline callback: (DataError) -> Unit = {}
): Result<T, DataError.Remote> {
    return when (this) {
        is Result.Error -> {

            if (error.type == DataError.Remote.Type.UNAUTHORIZED && !skipAuth) {
                UiEventController.send(UiEvent.SessionExpired)
            } else {
                val msg = when (error.type) {
                    DataError.Remote.Type.NO_INTERNET -> "Weak or no Internet connection"
                    DataError.Remote.Type.REQUEST_TIMEOUT -> "Request timed out"
                    DataError.Remote.Type.UNAUTHORIZED -> error.message ?: "Invalid username or password"
                    DataError.Remote.Type.CONFLICT -> error.message ?: "Username already exists"
                    DataError.Remote.Type.BAD_REQUEST -> error.message ?: "Invalid request"
                    DataError.Remote.Type.UNKNOWN -> error.message ?: "Something went wrong"
                    DataError.Remote.Type.PERMISSION_DENIED -> "Location Permission is Required"
                    DataError.Remote.Type.LOCATION_UNAVAILABLE -> error.message
                    DataError.Remote.Type.SERVER_ERROR -> "Server error, please try again later"
                    else -> error.message
                }
                msg?.let {
                    UiEventController.send(
                        UiEvent.Snackbar(
                            message = msg,
                            onAction = {
                                callback(error)
                            }
                        )
                    )
                }
            }

            this
        }

        is Result.Success -> this
    }
}