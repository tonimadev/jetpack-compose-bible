package digital.tonima.bibliadigital.core.common.core.network

sealed class NetworkError {
    object ClientError : NetworkError()

    object ServerError : NetworkError()

    data class Unexpected(val message: String?) : NetworkError()

    companion object {
        fun fromException(e: Exception): NetworkError = Unexpected(e.message)
    }
}
