package digital.tonima.bibliadigital.domain.usecases

import digital.tonima.bibliadigital.data.datastore.ReadingHistory
import digital.tonima.bibliadigital.domain.BibleDomainEffects
import digital.tonima.bibliadigital.domain.core.computation.CapabilityRegistry
import digital.tonima.bibliadigital.domain.core.computation.Computation
import digital.tonima.bibliadigital.domain.core.exception.Failure
import digital.tonima.bibliadigital.domain.core.function.Either
import javax.inject.Inject

class GetReadingHistoryUseCase
    @Inject
    constructor(private val bibleDomainEffects: BibleDomainEffects) : UseCase<ReadingHistory?, UseCase.None>() {
        override fun execute(params: None): Computation<CapabilityRegistry, Either<Failure, ReadingHistory?>> =
            bibleDomainEffects.getReadingHistory()
    }
