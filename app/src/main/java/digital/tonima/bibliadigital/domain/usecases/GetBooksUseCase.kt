package digital.tonima.bibliadigital.domain.usecases

import digital.tonima.bibliadigital.domain.core.exception.Failure
import digital.tonima.bibliadigital.domain.core.function.Either
import digital.tonima.bibliadigital.domain.model.BookResponse
import digital.tonima.bibliadigital.domain.repository.BibleRepository
import javax.inject.Inject

class GetBooksUseCase
    @Inject
    constructor(
        private val bibleRepository: BibleRepository,
    ) : UseCase<List<BookResponse>, UseCase.None>() {
        override suspend fun run(params: None): Either<Failure, List<BookResponse>> {
            return try {
                bibleRepository.getBooks()
            } catch (ex: Exception) {
                Either.Fail(Failure.Error)
            }
        }
    }
