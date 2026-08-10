package digital.tonima.bibliadigital.domain.usecases

import digital.tonima.bibliadigital.data.datastore.PreferencesDataStore
import digital.tonima.bibliadigital.domain.core.exception.Failure
import digital.tonima.bibliadigital.domain.core.function.Either
import javax.inject.Inject

class StoreReadingHistoryUseCase
    @Inject
    constructor(
        private val preferencesDataStore: PreferencesDataStore,
    ) : UseCase<Unit, StoreReadingHistoryUseCase.Params>() {
        override suspend fun run(params: Params): Either<Failure, Unit> {
            return preferencesDataStore.storeReadingHistory(
                params.bookName,
                params.bookAbbrev,
                params.chapterId,
                params.chapterQuantity,
            )
        }

        data class Params(
            val bookName: String,
            val bookAbbrev: String,
            val chapterId: Int,
            val chapterQuantity: Int,
        )
    }
