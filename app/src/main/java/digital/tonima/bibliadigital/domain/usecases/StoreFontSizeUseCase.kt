package digital.tonima.bibliadigital.domain.usecases

import digital.tonima.bibliadigital.domain.BibleDomainEffects
import digital.tonima.bibliadigital.domain.core.computation.CapabilityRegistry
import digital.tonima.bibliadigital.domain.core.computation.Computation
import digital.tonima.bibliadigital.domain.core.exception.Failure
import digital.tonima.bibliadigital.domain.core.function.Either
import javax.inject.Inject

class StoreFontSizeUseCase
    @Inject
    constructor(private val bibleDomainEffects: BibleDomainEffects) :
    UseCase<Unit, StoreFontSizeUseCase.Params>() {
        override fun execute(params: Params): Computation<CapabilityRegistry, Either<Failure, Unit>> =
            bibleDomainEffects.storeFontSize(params.fontSize)

        data class Params(val fontSize: Int)
    }
