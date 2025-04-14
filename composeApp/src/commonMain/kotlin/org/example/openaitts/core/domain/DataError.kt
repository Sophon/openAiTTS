package org.example.openaitts.core.domain

sealed interface DataError: Error {
    enum class Remote: DataError {
        UNKNOWN,
        TOO_MANY_REQUESTS,
        REQUEST_TIMEOUT,
        NO_INTERNET,
        SERVER_ERROR,
        SERIALIZATION_ERROR,
    }
}