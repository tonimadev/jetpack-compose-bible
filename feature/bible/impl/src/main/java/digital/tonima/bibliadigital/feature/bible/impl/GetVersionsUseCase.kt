package digital.tonima.bibliadigital.feature.bible.impl

import digital.tonima.bibliadigital.core.common.core.computation.CapabilityRegistry
import digital.tonima.bibliadigital.core.common.core.computation.Computation
import digital.tonima.bibliadigital.core.common.core.exception.Failure
import digital.tonima.bibliadigital.core.common.core.function.Either
import digital.tonima.bibliadigital.core.common.model.Version
import javax.inject.Inject

class GetVersionsUseCase
    @Inject
    constructor(private val bibleDomainEffects: BibleDomainEffects) : UseCase<List<Version>, UseCase.None>() {
        override fun execute(params: None): Computation<CapabilityRegistry, Either<Failure, List<Version>>> =
            bibleDomainEffects.getVersions()
    }
