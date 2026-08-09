package digital.tonima.bibliadigital.domain.repository

import digital.tonima.bibliadigital.data.local.room.ChurchDatabase
import digital.tonima.bibliadigital.data.remote.bible.ChurchRoomService
import digital.tonima.bibliadigital.domain.common.constants.BIBLE_BASE_URL
import digital.tonima.bibliadigital.domain.core.exception.Failure
import digital.tonima.bibliadigital.domain.core.function.Either
import digital.tonima.bibliadigital.domain.core.plataform.NetworkHandler
import digital.tonima.bibliadigital.domain.model.Abbrev
import digital.tonima.bibliadigital.domain.model.AbbrevRoomModel
import digital.tonima.bibliadigital.domain.model.BookResponse
import digital.tonima.bibliadigital.domain.model.ChapterResponse
import javax.inject.Inject

interface BibleRepository : Repository {
    suspend fun getBooks(): Either<Failure, List<BookResponse>>

    suspend fun getChapter(
        bookName: String,
        bookAbbrev: String,
        selectedChapter: Int,
    ): Either<Failure, ChapterResponse>

    class Network
        @Inject
        constructor(
            private val networkHandler: NetworkHandler,
            private val service: ChurchRoomService,
            private val churchDatabase: ChurchDatabase,
        ) : BibleRepository {
            private val verseUrl = BIBLE_BASE_URL + "verses/nvi/%s/%d"

            override suspend fun getBooks(): Either<Failure, List<BookResponse>> {
                val booksDao = churchDatabase.churchDao()

                val books = booksDao.getAllBooks()

                return if (books.isEmpty()) {
                    when (networkHandler.isNetworkAvailable()) {
                        true ->
                            request(
                                service.getBooks(),
                            ) { booksResponse ->
                                booksDao.insertAllBooks(booksResponse)
                                booksResponse.forEach {
                                    booksDao.insertAllAbbrevs(
                                        AbbrevRoomModel(
                                            bookName = it.name,
                                            abbrev = it.abbrev.pt,
                                        ),
                                    )
                                }
                                booksResponse
                            }
                        false -> Either.Fail(Failure.NetworkConnection)
                    }
                } else {
                    val abbrevs = booksDao.getAllAbbrevs()

                    books.forEach { bookResponse ->
                        abbrevs.firstOrNull { it.bookName == bookResponse.name }?.abbrev?.let {
                            bookResponse.abbrev = Abbrev(pt = it)
                        }
                    }

                    Either.Success(books.distinctBy { it.name })
                }
            }

            override suspend fun getChapter(
                bookName: String,
                bookAbbrev: String,
                selectedChapter: Int,
            ): Either<Failure, ChapterResponse> {
                val bibleDao = churchDatabase.churchDao()

                val chapterDbResponse =
                    bibleDao.getAllChapters()
                        .firstOrNull { it.book.name == bookName && it.chapter.number == selectedChapter }

                val abbrevs = bibleDao.getAllAbbrevs()
                val abbrev = abbrevs.firstOrNull { it.bookName == bookName }

                return if (chapterDbResponse == null) {
                    when (networkHandler.isNetworkAvailable()) {
                        true ->
                            request(
                                service.getChapter(verseUrl.format(abbrev?.abbrev, selectedChapter)),
                            ) { chapterResponse ->
                                bibleDao.insertAllChapters(chapterResponse)
                                chapterResponse
                            }
                        false -> Either.Fail(Failure.NetworkConnection)
                    }
                } else {
                    Either.Success(chapterDbResponse)
                }
            }
        }
}
