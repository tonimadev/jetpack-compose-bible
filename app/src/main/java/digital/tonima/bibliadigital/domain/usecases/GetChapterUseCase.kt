package digital.tonima.bibliadigital.domain.usecases

import digital.tonima.bibliadigital.domain.BibleDomainEffects
import digital.tonima.bibliadigital.domain.core.computation.CapabilityRegistry
import digital.tonima.bibliadigital.domain.core.computation.Computation
import digital.tonima.bibliadigital.domain.core.exception.Failure
import digital.tonima.bibliadigital.domain.core.function.Either
import digital.tonima.bibliadigital.domain.model.ChapterResponse
import digital.tonima.bibliadigital.domain.usecases.GetChapterUseCase.Params
import javax.inject.Inject

class GetChapterUseCase
    @Inject
    constructor(private val bibleDomainEffects: BibleDomainEffects) : UseCase<ChapterResponse, Params>() {
        override fun execute(params: Params): Computation<CapabilityRegistry, Either<Failure, ChapterResponse>> =
            bibleDomainEffects.getChapter(
                params.bookName,
                params.bookAbbrev,
                params.chapterId,
                params.version,
            )

        data class Params(
            val bookName: String,
            val bookAbbrev: String,
            val chapterId: Int,
            val version: String,
        )
    }
