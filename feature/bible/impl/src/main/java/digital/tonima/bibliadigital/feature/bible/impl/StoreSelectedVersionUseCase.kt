package digital.tonima.bibliadigital.feature.bible.impl

import digital.tonima.bibliadigital.core.common.core.computation.CapabilityRegistry
import digital.tonima.bibliadigital.core.common.core.computation.Computation
import digital.tonima.bibliadigital.core.common.core.exception.Failure
import digital.tonima.bibliadigital.core.common.core.function.Either
import digital.tonima.bibliadigital.feature.bible.impl.StoreSelectedVersionUseCase.Params
import javax.inject.Inject

class StoreSelectedVersionUseCase
    @Inject
    constructor(private val bibleDomainEffects: BibleDomainEffects) : UseCase<Unit, Params>() {
        override fun execute(params: Params): Computation<CapabilityRegistry, Either<Failure, Unit>> =
            bibleDomainEffects.storeSelectedVersion(params.version)

        data class Params(val version: String)
    }
