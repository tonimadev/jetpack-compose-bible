package digital.tonima.bibliadigital.domain.usecases

import digital.tonima.bibliadigital.data.datastore.PreferencesDataStore
import digital.tonima.bibliadigital.domain.core.exception.Failure
import digital.tonima.bibliadigital.domain.core.function.Either
import javax.inject.Inject

class GetFontSizeUseCase
    @Inject
    constructor(private val preferencesDataStore: PreferencesDataStore) :
    UseCase<Int, UseCase.None>() {
        override suspend fun run(params: None): Either<Failure, Int> = preferencesDataStore.readFontSize()
    }
