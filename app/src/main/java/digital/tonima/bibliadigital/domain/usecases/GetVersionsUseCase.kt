package digital.tonima.bibliadigital.domain.usecases

import digital.tonima.bibliadigital.domain.BibleDomainEffects
import digital.tonima.bibliadigital.domain.core.computation.CapabilityRegistry
import digital.tonima.bibliadigital.domain.core.computation.Computation
import digital.tonima.bibliadigital.domain.core.exception.Failure
import digital.tonima.bibliadigital.domain.core.function.Either
import digital.tonima.bibliadigital.domain.model.Version
import javax.inject.Inject

class GetVersionsUseCase
    @Inject
    constructor(private val bibleDomainEffects: BibleDomainEffects) : UseCase<List<Version>, UseCase.None>() {
        override fun execute(params: None): Computation<CapabilityRegistry, Either<Failure, List<Version>>> =
            bibleDomainEffects.getVersions()
    }
