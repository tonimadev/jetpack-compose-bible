package digital.tonima.bibliadigital.domain.usecases

import digital.tonima.bibliadigital.domain.BibleDomainEffects
import digital.tonima.bibliadigital.domain.core.computation.CapabilityRegistry
import digital.tonima.bibliadigital.domain.core.computation.Computation
import digital.tonima.bibliadigital.domain.core.exception.Failure
import digital.tonima.bibliadigital.domain.core.function.Either
import digital.tonima.bibliadigital.domain.model.Book
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
