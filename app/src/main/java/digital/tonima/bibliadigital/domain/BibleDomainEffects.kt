package digital.tonima.bibliadigital.domain

import digital.tonima.bibliadigital.data.datastore.PreferencesDataStore
import digital.tonima.bibliadigital.data.datastore.ReadingHistory
import digital.tonima.bibliadigital.data.local.room.ChurchDao
import digital.tonima.bibliadigital.data.remote.bible.BibleEffects
import digital.tonima.bibliadigital.data.remote.bible.ChurchRoomApi
import digital.tonima.bibliadigital.domain.common.constants.BIBLE_CHAPTERS_COUNT
import digital.tonima.bibliadigital.domain.core.computation.CapabilityRegistry
import digital.tonima.bibliadigital.domain.core.computation.Computation
import digital.tonima.bibliadigital.domain.core.exception.Failure
import digital.tonima.bibliadigital.domain.core.exception.Failure.Error
import digital.tonima.bibliadigital.domain.core.exception.Failure.NetworkConnection
import digital.tonima.bibliadigital.domain.core.function.Either
import digital.tonima.bibliadigital.domain.core.function.Either.Fail
import digital.tonima.bibliadigital.domain.core.function.Either.Success
import digital.tonima.bibliadigital.domain.core.network.NetworkError
import digital.tonima.bibliadigital.domain.core.plataform.NetworkHandler
import digital.tonima.bibliadigital.domain.model.AbbrevRoomModel
import digital.tonima.bibliadigital.domain.model.Book
import digital.tonima.bibliadigital.domain.model.ChapterResponse
import digital.tonima.bibliadigital.domain.model.FavoriteVerse
import digital.tonima.bibliadigital.domain.model.Version
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BibleDomainEffects
    @Inject
    constructor(
        private val bibleEffects: BibleEffects,
    ) {
        fun getBooks(): Computation<CapabilityRegistry, Either<NetworkError, List<Book>>> =
            Computation { registry ->
                val dao = registry.get(ChurchDao::class.java)
                val cached = dao.getAllBooks()

                if (cached.isNotEmpty()) {
                    val abbrevs = dao.getAllAbbrevs()
                    val enriched =
                        cached.map { dto ->
                            val abbrevStr = abbrevs.firstOrNull { it.bookName == dto.name }?.abbrev
                            val count = BIBLE_CHAPTERS_COUNT[(abbrevStr ?: dto.abbrev.pt).lowercase()] ?: dto.chapters
                            dto.copy(
                                abbrev = dto.abbrev.copy(pt = abbrevStr ?: dto.abbrev.pt),
                                chapters = count,
                            ).toDomain()
                        }
                    Success(enriched.distinctBy { it.name })
                } else {
                    val networkHandler = registry.get(NetworkHandler::class.java)
                    if (networkHandler.isNetworkAvailable()) {
                        val api = registry.get(ChurchRoomApi::class.java)
                        when (val result = bibleEffects.getBooks().runInContext(api)) {
                            is Fail -> result
                            is Success -> {
                                val dtos = result.b
                                val enrichedDtos =
                                    dtos.map { dto ->
                                        val count = BIBLE_CHAPTERS_COUNT[dto.abbrev.pt.lowercase()] ?: dto.chapters
                                        dto.copy(chapters = count)
                                    }
                                dao.insertAllBooks(enrichedDtos)
                                val abbrevs =
                                    enrichedDtos.map {
                                        AbbrevRoomModel(bookName = it.name, abbrev = it.abbrev.pt)
                                    }
                                dao.insertAllAbbrevs(abbrevs)
                                Success(enrichedDtos.map { it.toDomain() })
                            }
                        }
                    } else {
                        Fail(NetworkError.Unexpected("No network available"))
                    }
                }
            }

        fun getFontSize(): Computation<CapabilityRegistry, Either<Failure, Int>> =
            Computation { it.get(PreferencesDataStore::class.java).readFontSize() }

        fun storeFontSize(size: Int): Computation<CapabilityRegistry, Either<Failure, Unit>> =
            Computation { it.get(PreferencesDataStore::class.java).storeFontSize(size) }

        fun getVersions(): Computation<CapabilityRegistry, Either<Failure, List<Version>>> =
            Computation { registry ->
                val dao = registry.get(ChurchDao::class.java)
                val cached = dao.getAllVersions()
                if (cached.isNotEmpty()) {
                    Success(cached)
                } else {
                    val networkHandler = registry.get(NetworkHandler::class.java)
                    if (networkHandler.isNetworkAvailable()) {
                        val api = registry.get(ChurchRoomApi::class.java)
                        try {
                            val response = api.getVersions()
                            dao.insertAllVersions(response.data)
                            Success(response.data)
                        } catch (_: Exception) {
                            Fail(Error)
                        }
                    } else {
                        Fail(NetworkConnection)
                    }
                }
            }

        fun getSelectedVersion(): Computation<CapabilityRegistry, Either<Failure, String>> =
            Computation { it.get(PreferencesDataStore::class.java).readSelectedVersion() }

        fun storeSelectedVersion(version: String): Computation<CapabilityRegistry, Either<Failure, Unit>> =
            Computation { it.get(PreferencesDataStore::class.java).storeSelectedVersion(version) }

        fun getFavorites(): Computation<CapabilityRegistry, Either<Failure, List<FavoriteVerse>>> =
            Computation { Success(it.get(ChurchDao::class.java).getAllFavorites()) }

        fun toggleFavorite(
            bookName: String,
            chapter: Int,
            verseNumber: Int,
            text: String,
        ): Computation<CapabilityRegistry, Either<Failure, Boolean>> =
            Computation { registry ->
                try {
                    val dao = registry.get(ChurchDao::class.java)
                    val favorites = dao.getAllFavorites()
                    val existing =
                        favorites.find {
                            (it.bookName == bookName) && (it.chapter == chapter) && (it.verseNumber == verseNumber)
                        }
                    if (existing != null) {
                        dao.deleteFavorite(bookName, chapter, verseNumber)
                        Success(b = false)
                    } else {
                        dao.insertFavorite(
                            FavoriteVerse(
                                bookName = bookName,
                                chapter = chapter,
                                verseNumber = verseNumber,
                                text = text,
                            ),
                        )
                        Success(b = true)
                    }
                } catch (_: Exception) {
                    Fail(Error)
                }
            }

        fun getTutorialStatus(): Computation<CapabilityRegistry, Either<Failure, Boolean>> =
            Computation { it.get(PreferencesDataStore::class.java).readShowPressAndHoldVerseTutorial() }

        fun disableTutorial(): Computation<CapabilityRegistry, Either<Failure, Unit>> =
            Computation { it.get(PreferencesDataStore::class.java).disableShowPressAndHoldVerseTutorial() }

        fun getReadingHistory(): Computation<CapabilityRegistry, Either<Failure, ReadingHistory?>> =
            Computation { it.get(PreferencesDataStore::class.java).readReadingHistory() }

        fun storeReadingHistory(
            bookName: String,
            bookAbbrev: String,
            chapterId: Int,
            chapterQuantity: Int,
        ): Computation<CapabilityRegistry, Either<Failure, Unit>> =
            Computation {
                it.get(PreferencesDataStore::class.java)
                    .storeReadingHistory(bookName, bookAbbrev, chapterId, chapterQuantity)
            }

        fun getChapter(
            bookName: String,
            bookAbbrev: String,
            selectedChapter: Int,
            version: String,
        ): Computation<CapabilityRegistry, Either<Failure, ChapterResponse>> =
            Computation { registry ->
                val dao = registry.get(ChurchDao::class.java)
                val cached =
                    dao.getAllChapters().firstOrNull {
                        it.book.name.equals(bookName, ignoreCase = true) &&
                            it.chapter.number == selectedChapter &&
                            it.version.equals(version, ignoreCase = true)
                    }

                if (cached != null) {
                    Success(cached)
                } else {
                    val networkHandler = registry.get(NetworkHandler::class.java)
                    if (networkHandler.isNetworkAvailable()) {
                        val api = registry.get(ChurchRoomApi::class.java)
                        val abbrevs = dao.getAllAbbrevs()
                        val abbrev = abbrevs.firstOrNull { it.bookName == bookName }?.abbrev ?: bookAbbrev
                        try {
                            val response = api.getChapter(version, abbrev, selectedChapter)
                            val chapter = response.data.copy(version = version)
                            dao.insertAllChapters(listOf(chapter))
                            Success(chapter)
                        } catch (_: Exception) {
                            Fail(Error)
                        }
                    } else {
                        Fail(NetworkConnection)
                    }
                }
            }
    }
