package com.laarasoft.frontend.config.network

sealed interface DataError : Error {
    val message: String?

    data class Remote(
        val type: Type,
        override val message: String? = null
    ) : DataError {
        enum class Type {
            REQUEST_TIMEOUT,
            UNAUTHORIZED,
            CONFLICT,
            FILE_NOT_FOUND,
            TOO_MANY_REQUESTS,
            NO_INTERNET,
            PAYLOAD_TOO_LARGE,
            SERVER_ERROR,
            SERIALIZATION,
            UNKNOWN,
            CANCELLATION,
            PERMISSION_DENIED,
            LOCATION_UNAVAILABLE,
            NOT_FOUND,
            BAD_REQUEST,
        }
    }

    data class Local(
        val type: Type,
        override val message: String? = null
    ) : DataError {
        enum class Type {
            DISK_FULL,
            UNKNOWN,
            DATA_NOT_FOUND,
            DATABASE
        }
    }
}