package digital.tonima.bibliadigital.domain.core.computation

import digital.tonima.bibliadigital.domain.core.function.Either
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

class Computation<in C, out R>(val run: suspend (C) -> R) {
    suspend fun runInContext(capability: C): R = run(capability)

    fun <T> map(transform: (R) -> T): Computation<C, T> = Computation { capability -> transform(run(capability)) }

    fun <T, C2 : C> flatMap(transform: (R) -> Computation<C2, T>): Computation<C2, T> =
        Computation { capability ->
            val result = run(capability)
            transform(result).run(capability)
        }

    fun switchContext(coroutineContext: CoroutineContext): Computation<C, R> =
        Computation { capability ->
            withContext(coroutineContext) { run(capability) }
        }
}

fun <C, F, S, S2> Computation<C, Either<F, S>>.flatMapResult(
    transform: (S) -> Computation<C, Either<F, S2>>,
): Computation<C, Either<F, S2>> =
    Computation { capability ->
        when (val result = this.run(capability)) {
            is Either.Fail -> result
            is Either.Success -> transform(result.b).run(capability)
        }
    }

fun <C, F, S, S2> Computation<C, Either<F, S>>.mapResult(transform: (S) -> S2): Computation<C, Either<F, S2>> =
    this.map { either ->
        when (either) {
            is Either.Fail -> either
            is Either.Success -> Either.Success(transform(either.b))
        }
    }
