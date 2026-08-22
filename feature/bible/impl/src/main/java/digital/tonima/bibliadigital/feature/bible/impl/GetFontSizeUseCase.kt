package digital.tonima.bibliadigital.feature.bible.impl

import digital.tonima.bibliadigital.core.common.core.computation.CapabilityRegistry
import digital.tonima.bibliadigital.core.common.core.computation.Computation
import digital.tonima.bibliadigital.core.common.core.exception.Failure
import digital.tonima.bibliadigital.core.common.core.function.Either
import javax.inject.Inject

class GetFontSizeUseCase
    @Inject
    constructor(private val bibleDomainEffects: BibleDomainEffects) :
    UseCase<Int, UseCase.None>() {
        override fun execute(params: None): Computation<CapabilityRegistry, Either<Failure, Int>> =
            bibleDomainEffects.getFontSize()
    }
