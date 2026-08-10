package digital.tonima.bibliadigital.domain.usecases

import digital.tonima.bibliadigital.domain.core.exception.Failure
import digital.tonima.bibliadigital.domain.core.function.Either
import digital.tonima.bibliadigital.domain.model.Version
import digital.tonima.bibliadigital.domain.repository.BibleRepository
import javax.inject.Inject

class GetVersionsUseCase
    @Inject
    constructor(
        private val bibleRepository: BibleRepository,
    ) : UseCase<List<Version>, UseCase.None>() {
        override suspend fun run(params: None): Either<Failure, List<Version>> {
            return try {
                bibleRepository.getVersions()
            } catch (ex: Exception) {
                Either.Fail(Failure.Error)
            }
        }
    }
