package digital.tonima.bibliadigital.domain.usecases

import digital.tonima.bibliadigital.domain.core.computation.CapabilityRegistry
import digital.tonima.bibliadigital.domain.core.computation.Computation
import digital.tonima.bibliadigital.domain.core.exception.Failure
import digital.tonima.bibliadigital.domain.core.function.Either
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class UseCase<out Type, in Params> where Type : Any? {
    abstract fun execute(params: Params): Computation<CapabilityRegistry, Either<Failure, Type>>

    operator fun invoke(
        params: Params,
        registry: CapabilityRegistry,
        scope: CoroutineScope,
        onResult: (Either<Failure, Type>) -> Unit = {},
    ) {
        scope.launch(Dispatchers.Main) {
            val result =
                withContext(Dispatchers.IO) {
                    execute(params).runInContext(registry)
                }
            onResult(result)
        }
    }

    class None
}
