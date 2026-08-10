package digital.tonima.bibliadigital.domain.usecases

import digital.tonima.bibliadigital.data.datastore.PreferencesDataStore
import digital.tonima.bibliadigital.domain.core.exception.Failure
import digital.tonima.bibliadigital.domain.core.function.Either
import javax.inject.Inject

class StoreSelectedVersionUseCase
    @Inject
    constructor(
        private val preferencesDataStore: PreferencesDataStore,
    ) : UseCase<Unit, StoreSelectedVersionUseCase.Params>() {
        override suspend fun run(params: Params): Either<Failure, Unit> {
            return preferencesDataStore.storeSelectedVersion(params.version)
        }

        data class Params(val version: String)
    }
