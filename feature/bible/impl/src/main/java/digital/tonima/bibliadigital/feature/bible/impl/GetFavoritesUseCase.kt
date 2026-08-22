package digital.tonima.bibliadigital.feature.bible.impl

import digital.tonima.bibliadigital.core.common.core.computation.CapabilityRegistry
import digital.tonima.bibliadigital.core.common.core.computation.Computation
import digital.tonima.bibliadigital.core.common.core.exception.Failure
import digital.tonima.bibliadigital.core.common.core.function.Either
import digital.tonima.bibliadigital.core.common.model.FavoriteVerse
import javax.inject.Inject

class GetFavoritesUseCase
    @Inject
    constructor(private val bibleDomainEffects: BibleDomainEffects) : UseCase<List<FavoriteVerse>, UseCase.None>() {
        override fun execute(params: None): Computation<CapabilityRegistry, Either<Failure, List<FavoriteVerse>>> =
            bibleDomainEffects.getFavorites()
    }
