package digital.tonima.bibliadigital.domain.usecases

import digital.tonima.bibliadigital.data.local.room.ChurchDatabase
import digital.tonima.bibliadigital.domain.core.exception.Failure
import digital.tonima.bibliadigital.domain.core.function.Either
import digital.tonima.bibliadigital.domain.model.FavoriteVerse
import javax.inject.Inject

class ToggleFavoriteUseCase
    @Inject
    constructor(
        private val database: ChurchDatabase,
    ) : UseCase<Boolean, ToggleFavoriteUseCase.Params>() {
        override suspend fun run(params: Params): Either<Failure, Boolean> {
            return try {
                val dao = database.churchDao()
                val favorites = dao.getAllFavorites()
                val existing =
                    favorites.find {
                        it.bookName == params.bookName &&
                            it.chapter == params.chapter &&
                            it.verseNumber == params.verseNumber
                    }

                if (existing != null) {
                    dao.deleteFavorite(params.bookName, params.chapter, params.verseNumber)
                    Either.Success(false)
                } else {
                    dao.insertFavorite(
                        FavoriteVerse(
                            bookName = params.bookName,
                            chapter = params.chapter,
                            verseNumber = params.verseNumber,
                            text = params.text,
                        ),
                    )
                    Either.Success(true)
                }
            } catch (e: Exception) {
                Either.Fail(Failure.Error)
            }
        }

        data class Params(
            val bookName: String,
            val chapter: Int,
            val verseNumber: Int,
            val text: String,
        )
    }
