package digital.tonima.bibliadigital.domain.usecases

import digital.tonima.bibliadigital.data.local.room.ChurchDatabase
import digital.tonima.bibliadigital.domain.core.exception.Failure
import digital.tonima.bibliadigital.domain.core.function.Either
import digital.tonima.bibliadigital.domain.model.FavoriteVerse
import javax.inject.Inject

class GetFavoritesUseCase
    @Inject
    constructor(
        private val database: ChurchDatabase,
    ) : UseCase<List<FavoriteVerse>, UseCase.None>() {
        override suspend fun run(params: None): Either<Failure, List<FavoriteVerse>> {
            return try {
                Either.Success(database.churchDao().getAllFavorites())
            } catch (e: Exception) {
                Either.Fail(Failure.Error)
            }
        }
    }
