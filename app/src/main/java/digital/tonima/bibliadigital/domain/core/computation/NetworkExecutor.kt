package digital.tonima.bibliadigital.domain.core.computation

import digital.tonima.bibliadigital.domain.core.function.Either
import digital.tonima.bibliadigital.domain.core.network.NetworkError

interface NetworkExecutor {
    fun <C : Api, T> (suspend C.() -> T).effect(): Computation<C, Either<NetworkError, T>> =
        Computation { capability ->
            try {
                Either.Success(this(capability))
            } catch (e: Exception) {
                Either.Fail(NetworkError.fromException(e))
            }
        }
}
