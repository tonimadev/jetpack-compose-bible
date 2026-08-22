package digital.tonima.bibliadigital.feature.bible.impl

import digital.tonima.bibliadigital.core.common.core.computation.CapabilityRegistry
import digital.tonima.bibliadigital.core.common.core.computation.Computation
import digital.tonima.bibliadigital.core.common.core.exception.Failure
import digital.tonima.bibliadigital.core.common.core.function.Either
import digital.tonima.bibliadigital.core.common.model.ChapterResponse
import digital.tonima.bibliadigital.feature.bible.impl.GetChapterUseCase.Params
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
