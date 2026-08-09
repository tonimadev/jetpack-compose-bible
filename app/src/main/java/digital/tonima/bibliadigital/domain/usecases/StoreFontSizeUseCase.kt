package digital.tonima.bibliadigital.domain.usecases

import digital.tonima.bibliadigital.data.datastore.PreferencesDataStore
import digital.tonima.bibliadigital.domain.core.exception.Failure
import digital.tonima.bibliadigital.domain.core.function.Either
import javax.inject.Inject

class StoreFontSizeUseCase
    @Inject
    constructor(private val preferencesDataStore: PreferencesDataStore) :
    UseCase<Unit, StoreFontSizeUseCase.Params>() {
        override suspend fun run(params: Params): Either<Failure, Unit> =
            preferencesDataStore.storeFontSize(params.fontSize)

        data class Params(val fontSize: Int)
    }
