package digital.tonima.bibliadigital.feature.bible.impl

import digital.tonima.bibliadigital.core.common.core.computation.CapabilityRegistry
import digital.tonima.bibliadigital.core.common.core.computation.Computation
import digital.tonima.bibliadigital.core.common.core.exception.Failure
import digital.tonima.bibliadigital.core.common.core.function.Either
import javax.inject.Inject

class ToggleFavoriteUseCase
    @Inject
    constructor(private val bibleDomainEffects: BibleDomainEffects) : UseCase<Boolean, ToggleFavoriteUseCase.Params>() {
        override fun execute(params: Params): Computation<CapabilityRegistry, Either<Failure, Boolean>> =
            bibleDomainEffects.toggleFavorite(
                params.bookName,
                params.chapter,
                params.verseNumber,
                params.text,
            )

        data class Params(
            val bookName: String,
            val chapter: Int,
            val verseNumber: Int,
            val text: String,
        )
    }
