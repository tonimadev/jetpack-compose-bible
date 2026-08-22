package digital.tonima.bibliadigital.feature.bible.impl

import digital.tonima.bibliadigital.core.common.core.computation.CapabilityRegistry
import digital.tonima.bibliadigital.core.common.core.computation.Computation
import digital.tonima.bibliadigital.core.common.core.exception.Failure
import digital.tonima.bibliadigital.core.common.core.function.Either
import digital.tonima.bibliadigital.feature.bible.impl.StoreReadingHistoryUseCase.Params
import javax.inject.Inject

class StoreReadingHistoryUseCase
    @Inject
    constructor(private val bibleDomainEffects: BibleDomainEffects) : UseCase<Unit, Params>() {
        override fun execute(params: Params): Computation<CapabilityRegistry, Either<Failure, Unit>> =
            bibleDomainEffects.storeReadingHistory(
                params.bookName,
                params.bookAbbrev,
                params.chapterId,
                params.chapterQuantity,
            )

        data class Params(
            val bookName: String,
            val bookAbbrev: String,
            val chapterId: Int,
            val chapterQuantity: Int,
        )
    }
