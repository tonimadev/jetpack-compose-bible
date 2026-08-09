package digital.tonima.bibliadigital.domain.usecases

import digital.tonima.bibliadigital.data.datastore.PreferencesDataStore
import digital.tonima.bibliadigital.domain.core.exception.Failure
import digital.tonima.bibliadigital.domain.core.function.Either
import javax.inject.Inject

class GetShowPressAndHoldVerseTutorialUseCase
    @Inject
    constructor(
        private val preferencesDataStore: PreferencesDataStore,
    ) :
    UseCase<Boolean, UseCase.None>() {
        override suspend fun run(params: None): Either<Failure, Boolean> =
            preferencesDataStore.readShowPressAndHoldVerseTutorial()
    }
