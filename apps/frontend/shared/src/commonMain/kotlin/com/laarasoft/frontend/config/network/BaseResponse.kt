package com.laarasoft.frontend.config.network

import io.ktor.client.call.body
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ResponseException
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import kotlinx.io.IOException
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.coroutines.cancellation.CancellationException

@Serializable
data class BaseResponse<T>(
    val statusCode: Int = -1,
    val data: T? = null,
    val message: String? = null,
    @SerialName("isSuccess")
    val success: Boolean = false
)



suspend inline fun <reified T> responseToResult(
    isNotByData: Boolean,
    response: HttpResponse,
): Result<T, DataError.Remote> {
    return when (response.status.value) {
        in 200..299 -> {
            try {
                if(isNotByData){
                    return Result.Success(response.body())
                }

                val bodyText = response.bodyAsText()

                val json = Json {
                    ignoreUnknownKeys = true
                    explicitNulls = false
                    isLenient = true
                    prettyPrint = true
                    coerceInputValues = true
                }

                val parsed = json.decodeFromString<BaseResponse<T>>(bodyText)

                if (parsed.success && parsed.statusCode in setOf(200, 201)) {

                    when {
                        parsed.data != null -> Result.Success(parsed.data)

                        T::class == Unit::class -> {
                            @Suppress("UNCHECKED_CAST")
                            Result.Success(Unit as T)
                        }

                        else -> Result.Error(
                            DataError.Remote(
                                type = DataError.Remote.Type.SERIALIZATION,
                                message = "Expected data but got null"
                            )
                        )
                    }
                } else {
                    Result.Error(
                        DataError.Remote(
                            type = DataError.Remote.Type.UNKNOWN,
                            message = parsed.message
                        )
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Result.Error(
                    DataError.Remote(
                        type = DataError.Remote.Type.SERIALIZATION,
                        message = "${e.message}"
                    )
                )
            }
        }

        408 -> Result.Error(DataError.Remote(DataError.Remote.Type.REQUEST_TIMEOUT))
        401 -> {
            try {
                val errorText = response.bodyAsText()
                val json = Json {
                    ignoreUnknownKeys = true
                    explicitNulls = false
                    isLenient = true
                    prettyPrint = true
                    coerceInputValues = true
                }
                val parsed = json.decodeFromString<BaseResponse<kotlinx.serialization.json.JsonElement>>(errorText)

                Result.Error(
                    DataError.Remote(
                        type = DataError.Remote.Type.UNAUTHORIZED,
                        message = parsed.message
                    )
                )
            } catch (e: Exception) {
                Result.Error(
                    DataError.Remote(
                        type = DataError.Remote.Type.UNAUTHORIZED,
                        message = "Unexpected error with no readable message"
                    )
                )
            }
        }
        409 -> Result.Error(DataError.Remote(DataError.Remote.Type.CONFLICT))
        429 -> Result.Error(DataError.Remote(DataError.Remote.Type.TOO_MANY_REQUESTS))
        in 500..599 -> Result.Error(DataError.Remote(DataError.Remote.Type.SERVER_ERROR))

        else -> {
            try {
                val errorText = response.bodyAsText()
                val json = Json {
                    ignoreUnknownKeys = true
                    explicitNulls = false
                    isLenient = true
                    prettyPrint = true
                    coerceInputValues = true
                }
                val parsed = json.decodeFromString<BaseResponse<kotlinx.serialization.json.JsonElement>>(errorText)

                Result.Error(
                    DataError.Remote(
                        type = DataError.Remote.Type.UNKNOWN,
                        message = parsed.message
                    )
                )
            } catch (e: Exception) {
                Result.Error(
                    DataError.Remote(
                        type = DataError.Remote.Type.UNKNOWN,
                        message = "Unexpected error with no readable message"
                    )
                )
            }
        }
    }
}




suspend inline fun <reified T> safeCall(
    isNotByData: Boolean = false,
    isDecrypt: Boolean = false,
    execute: () -> HttpResponse
): Result<T, DataError.Remote> {

    val response = try {
        execute()
    } catch (e: HttpRequestTimeoutException) {
        return Result.Error(
            DataError.Remote(
                type = DataError.Remote.Type.REQUEST_TIMEOUT,
                message = e.message
            )
        )
    } catch (e: ConnectTimeoutException) {
        return Result.Error(
            DataError.Remote(
                type = DataError.Remote.Type.REQUEST_TIMEOUT,
                message = e.message
            )
        )
    } catch (e: IOException) {
        return Result.Error(
            DataError.Remote(
                type = DataError.Remote.Type.NO_INTERNET,
                message = e.message
            )
        )
    } catch (e: CancellationException) {
        throw e
    } catch (e: ResponseException) {   // 4xx,5xx
        return responseToResult(
            response = e.response,
            isNotByData = isNotByData,
        )
    } catch (e: Exception) {
        return Result.Error(
            DataError.Remote(
                type = DataError.Remote.Type.UNKNOWN,
                message = e.message ?: "Unexpected error"
            )
        )
    }

    return responseToResult(
        response = response,
        isNotByData = isNotByData,
    )
}