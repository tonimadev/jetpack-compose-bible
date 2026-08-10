package digital.tonima.bibliadigital.domain.repository

import digital.tonima.bibliadigital.data.local.room.ChurchDatabase
import digital.tonima.bibliadigital.data.remote.bible.ChurchRoomService
import digital.tonima.bibliadigital.domain.common.constants.BIBLE_CHAPTERS_COUNT
import digital.tonima.bibliadigital.domain.core.exception.Failure
import digital.tonima.bibliadigital.domain.core.function.Either
import digital.tonima.bibliadigital.domain.core.plataform.NetworkHandler
import digital.tonima.bibliadigital.domain.model.AbbrevRoomModel
import digital.tonima.bibliadigital.domain.model.BookResponse
import digital.tonima.bibliadigital.domain.model.ChapterResponse
import digital.tonima.bibliadigital.domain.model.Version
import timber.log.Timber
import javax.inject.Inject

interface BibleRepository : Repository {
    suspend fun getBooks(): Either<Failure, List<BookResponse>>

    suspend fun getVersions(): Either<Failure, List<Version>>

    suspend fun getChapter(
        bookName: String,
        bookAbbrev: String,
        selectedChapter: Int,
        version: String,
    ): Either<Failure, ChapterResponse>

    class Network
        @Inject
        constructor(
            private val networkHandler: NetworkHandler,
            private val service: ChurchRoomService,
            private val churchDatabase: ChurchDatabase,
        ) : BibleRepository {
            override suspend fun getBooks(): Either<Failure, List<BookResponse>> {
                val booksDao = churchDatabase.churchDao()

                val books = booksDao.getAllBooks()

                return if (books.isEmpty()) {
                    when (networkHandler.isNetworkAvailable()) {
                        true ->
                            request(
                                service.getBooks(),
                            ) { baseResponse ->
                                Timber.d("getBooks response: $baseResponse")
                                val booksResponse =
                                    baseResponse.data.map { book ->
                                        val count = BIBLE_CHAPTERS_COUNT[book.abbrev] ?: 0
                                        if (count == 0) Timber.w("Chapter count not found for ${book.abbrev}")
                                        book.copy(chapters = count)
                                    }
                                booksDao.insertAllBooks(booksResponse)
                                val abbrevs =
                                    booksResponse.map {
                                        AbbrevRoomModel(
                                            bookName = it.name,
                                            abbrev = it.abbrev,
                                        )
                                    }
                                booksDao.insertAllAbbrevs(abbrevs)
                                booksResponse
                            }
                        false -> Either.Fail(Failure.NetworkConnection)
                    }
                } else {
                    val abbrevs = booksDao.getAllAbbrevs()

                    val mappedBooks =
                        books.map { bookResponse ->
                            val abbrevStr = abbrevs.firstOrNull { it.bookName == bookResponse.name }?.abbrev
                            val chapterCount =
                                BIBLE_CHAPTERS_COUNT[abbrevStr ?: bookResponse.abbrev] ?: bookResponse.chapters
                            if (chapterCount == 0) {
                                Timber.w(
                                    "Cached chapter count is 0 for ${bookResponse.name}",
                                )
                            }
                            bookResponse.copy(
                                abbrev = abbrevStr ?: bookResponse.abbrev,
                                chapters = chapterCount,
                            )
                        }

                    Either.Success(mappedBooks.distinctBy { it.name })
                }
            }

            override suspend fun getVersions(): Either<Failure, List<Version>> {
                return when (networkHandler.isNetworkAvailable()) {
                    true ->
                        request(
                            service.getVersions(),
                        ) { it.data }
                    false -> Either.Fail(Failure.NetworkConnection)
                }
            }

            override suspend fun getChapter(
                bookName: String,
                bookAbbrev: String,
                selectedChapter: Int,
                version: String,
            ): Either<Failure, ChapterResponse> {
                val bibleDao = churchDatabase.churchDao()

                val chapterDbResponse =
                    bibleDao.getAllChapters()
                        .firstOrNull {
                            it.book.name == bookName &&
                                it.chapter.number == selectedChapter &&
                                it.version.lowercase() == version.lowercase()
                        }

                val abbrevs = bibleDao.getAllAbbrevs()
                val abbrev = abbrevs.firstOrNull { it.bookName == bookName }

                return if (chapterDbResponse == null) {
                    when (networkHandler.isNetworkAvailable()) {
                        true ->
                            request(
                                service.getChapter(
                                    version = version,
                                    book = abbrev?.abbrev ?: bookAbbrev,
                                    chapter = selectedChapter,
                                ),
                            ) { chapterResponse ->
                                val chapter = chapterResponse.data.copy(version = version)
                                bibleDao.insertAllChapters(listOf(chapter))
                                chapter
                            }
                        false -> Either.Fail(Failure.NetworkConnection)
                    }
                } else {
                    Either.Success(chapterDbResponse)
                }
            }
        }
}
