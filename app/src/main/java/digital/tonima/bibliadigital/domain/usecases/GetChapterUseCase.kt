package digital.tonima.bibliadigital.domain.usecases

import digital.tonima.bibliadigital.domain.core.exception.Failure
import digital.tonima.bibliadigital.domain.core.function.Either
import digital.tonima.bibliadigital.domain.model.ChapterResponse
import digital.tonima.bibliadigital.domain.repository.BibleRepository
import javax.inject.Inject

class GetChapterUseCase
    @Inject
    constructor(
        private val bibleRepository: BibleRepository,
    ) : UseCase<ChapterResponse, GetChapterUseCase.Params>() {
        override suspend fun run(params: Params): Either<Failure, ChapterResponse> =
            bibleRepository.getChapter(params.bookName, params.bookAbbrev, params.chapterId)

        data class Params(
            val bookName: String,
            val bookAbbrev: String,
            val chapterId: Int,
        )
    }
