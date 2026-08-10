package digital.tonima.bibliadigital.domain.usecases

import digital.tonima.bibliadigital.data.datastore.PreferencesDataStore
import digital.tonima.bibliadigital.data.datastore.ReadingHistory
import digital.tonima.bibliadigital.domain.core.exception.Failure
import digital.tonima.bibliadigital.domain.core.function.Either
import javax.inject.Inject

class GetReadingHistoryUseCase
    @Inject
    constructor(
        private val preferencesDataStore: PreferencesDataStore,
    ) : UseCase<ReadingHistory?, UseCase.None>() {
        override suspend fun run(params: None): Either<Failure, ReadingHistory?> {
            return preferencesDataStore.readReadingHistory()
        }
    }
