package digital.tonima.bibliadigital.domain.repository

import digital.tonima.bibliadigital.domain.core.exception.Failure
import digital.tonima.bibliadigital.domain.core.function.Either
import retrofit2.Call
import timber.log.Timber

interface Repository {
    fun <T, R> request(
        call: Call<T>,
        transform: (T) -> R,
    ): Either<Failure, R> {
        return try {
            val response = call.execute()
            val either =
                when (response.isSuccessful) {
                    true -> Either.Success(transform((response.body()!!)))
                    false -> Either.Fail(Failure.ServerError)
                }
            either
        } catch (exception: Throwable) {
            Timber.e(exception)
            Either.Fail(Failure.ServerError)
        }
    }
}
