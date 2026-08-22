package digital.tonima.bibliadigital.feature.bible.impl

import digital.tonima.bibliadigital.core.common.core.computation.CapabilityRegistry
import digital.tonima.bibliadigital.core.common.core.computation.Computation
import digital.tonima.bibliadigital.core.common.core.exception.Failure
import digital.tonima.bibliadigital.core.common.core.function.Either
import javax.inject.Inject

class StoreFontSizeUseCase
    @Inject
    constructor(private val bibleDomainEffects: BibleDomainEffects) :
    UseCase<Unit, StoreFontSizeUseCase.Params>() {
        override fun execute(params: Params): Computation<CapabilityRegistry, Either<Failure, Unit>> =
            bibleDomainEffects.storeFontSize(params.fontSize)

        data class Params(val fontSize: Int)
    }
