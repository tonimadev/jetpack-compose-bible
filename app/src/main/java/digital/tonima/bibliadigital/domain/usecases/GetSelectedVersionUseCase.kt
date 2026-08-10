package digital.tonima.bibliadigital.domain.usecases

import digital.tonima.bibliadigital.data.datastore.PreferencesDataStore
import digital.tonima.bibliadigital.domain.core.exception.Failure
import digital.tonima.bibliadigital.domain.core.function.Either
import javax.inject.Inject

class GetSelectedVersionUseCase
    @Inject
    constructor(
        private val preferencesDataStore: PreferencesDataStore,
    ) : UseCase<String, UseCase.None>() {
        override suspend fun run(params: None): Either<Failure, String> {
            return preferencesDataStore.readSelectedVersion()
        }
    }
