package digital.tonima.bibliadigital.feature.bible.impl

import digital.tonima.bibliadigital.core.common.core.computation.CapabilityRegistry
import digital.tonima.bibliadigital.core.common.core.computation.Computation
import digital.tonima.bibliadigital.core.common.core.exception.Failure
import digital.tonima.bibliadigital.core.common.core.function.Either
import digital.tonima.bibliadigital.core.common.model.Book
import javax.inject.Inject

class GetBooksUseCase
    @Inject
    constructor(private val bibleDomainEffects: BibleDomainEffects) : UseCase<List<Book>, UseCase.None>() {
        override fun execute(params: None): Computation<CapabilityRegistry, Either<Failure, List<Book>>> =
            bibleDomainEffects.getBooks().map { either ->
                when (either) {
                    is Either.Fail -> Either.Fail(Failure.ServerError) // Map NetworkError to Failure
                    is Either.Success -> Either.Success(either.b)
                }
            }
    }
