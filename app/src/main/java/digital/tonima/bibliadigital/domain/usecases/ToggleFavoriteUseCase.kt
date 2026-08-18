package digital.tonima.bibliadigital.domain.usecases

import digital.tonima.bibliadigital.domain.BibleDomainEffects
import digital.tonima.bibliadigital.domain.core.computation.CapabilityRegistry
import digital.tonima.bibliadigital.domain.core.computation.Computation
import digital.tonima.bibliadigital.domain.core.exception.Failure
import digital.tonima.bibliadigital.domain.core.function.Either
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
