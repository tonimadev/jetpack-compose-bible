package digital.tonima.bibliadigital.data.remote.bible

import digital.tonima.bibliadigital.domain.core.computation.Computation
import digital.tonima.bibliadigital.domain.core.computation.NetworkExecutor
import digital.tonima.bibliadigital.domain.core.computation.mapResult
import digital.tonima.bibliadigital.domain.core.function.Either
import digital.tonima.bibliadigital.domain.core.network.NetworkError
import digital.tonima.bibliadigital.domain.model.BibleChapter
import digital.tonima.bibliadigital.domain.model.BookResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BibleEffects
    @Inject
    constructor() : NetworkExecutor {
        fun getBooks(): Computation<ChurchRoomApi, Either<NetworkError, List<BookResponse>>> =
            ChurchRoomApi::getBooks.effect()
                .mapResult { response -> response.data }

        fun getChapter(
            version: String,
            book: String,
            chapter: Int,
        ): Computation<ChurchRoomApi, Either<NetworkError, BibleChapter>> =
            Computation<ChurchRoomApi, Either<NetworkError, BibleChapter>> { api ->
                try {
                    Either.Success(api.getChapter(version, book, chapter).data.toDomain())
                } catch (e: Exception) {
                    Either.Fail(NetworkError.fromException(e))
                }
            }
    }
