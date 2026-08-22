package digital.tonima.bibliadigital.core.network

import digital.tonima.bibliadigital.core.common.core.computation.Computation
import digital.tonima.bibliadigital.core.common.core.computation.NetworkExecutor
import digital.tonima.bibliadigital.core.common.core.computation.mapResult
import digital.tonima.bibliadigital.core.common.core.function.Either
import digital.tonima.bibliadigital.core.common.core.network.NetworkError
import digital.tonima.bibliadigital.core.common.model.BibleChapter
import digital.tonima.bibliadigital.core.common.model.BookResponse
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
