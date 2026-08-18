package digital.tonima.bibliadigital.domain.usecases

import digital.tonima.bibliadigital.domain.BibleDomainEffects
import digital.tonima.bibliadigital.domain.core.computation.CapabilityRegistry
import digital.tonima.bibliadigital.domain.core.computation.Computation
import digital.tonima.bibliadigital.domain.core.exception.Failure
import digital.tonima.bibliadigital.domain.core.function.Either
import digital.tonima.bibliadigital.domain.usecases.StoreSelectedVersionUseCase.Params
import javax.inject.Inject

class StoreSelectedVersionUseCase
    @Inject
    constructor(private val bibleDomainEffects: BibleDomainEffects) : UseCase<Unit, Params>() {
        override fun execute(params: Params): Computation<CapabilityRegistry, Either<Failure, Unit>> =
            bibleDomainEffects.storeSelectedVersion(params.version)

        data class Params(val version: String)
    }
