package digital.tonima.bibliadigital.data.local.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import digital.tonima.bibliadigital.data.local.room.ChurchDatabase.Companion.ABBREVS_TABLE
import digital.tonima.bibliadigital.data.local.room.ChurchDatabase.Companion.BOOKS_TABLE
import digital.tonima.bibliadigital.data.local.room.ChurchDatabase.Companion.CHAPTERS_TABLE
import digital.tonima.bibliadigital.data.local.room.ChurchDatabase.Companion.VERSIONS_TABLE
import digital.tonima.bibliadigital.domain.core.computation.Database
import digital.tonima.bibliadigital.domain.model.AbbrevRoomModel
import digital.tonima.bibliadigital.domain.model.BookResponse
import digital.tonima.bibliadigital.domain.model.ChapterResponse
import digital.tonima.bibliadigital.domain.model.FavoriteVerse
import digital.tonima.bibliadigital.domain.model.Version

@Dao
interface ChurchDao : Database {
    @Query(ALL_CHAPTERS_QUERY)
    suspend fun getAllChapters(): List<ChapterResponse>

    @Query(ALL_BOOKS_QUERY)
    suspend fun getAllBooks(): List<BookResponse>

    @Query(ALL_VERSIONS_QUERY)
    suspend fun getAllVersions(): List<Version>

    @Query(ALL_ABBREVS_QUERY)
    suspend fun getAllAbbrevs(): List<AbbrevRoomModel>

    @Query("SELECT * FROM favorites ORDER BY timestamp DESC")
    suspend fun getAllFavorites(): List<FavoriteVerse>

    @Insert(onConflict = REPLACE)
    suspend fun insertAllBooks(books: List<BookResponse>)

    @Insert(onConflict = REPLACE)
    suspend fun insertAllVersions(versions: List<Version>)

    @Insert(onConflict = REPLACE)
    suspend fun insertAllChapters(chapters: List<ChapterResponse>)

    @Insert(onConflict = REPLACE)
    suspend fun insertAllAbbrevs(abbrevs: List<AbbrevRoomModel>)

    @Insert(onConflict = REPLACE)
    suspend fun insertFavorite(favorite: FavoriteVerse)

    @Query("DELETE FROM favorites WHERE bookName = :bookName AND chapter = :chapter AND verseNumber = :verseNumber")
    suspend fun deleteFavorite(
        bookName: String,
        chapter: Int,
        verseNumber: Int,
    )

    companion object {
        const val ALL_CHAPTERS_QUERY = "SELECT * FROM $CHAPTERS_TABLE"
        const val ALL_BOOKS_QUERY = "SELECT * FROM $BOOKS_TABLE"
        const val ALL_VERSIONS_QUERY = "SELECT * FROM $VERSIONS_TABLE"
        const val ALL_ABBREVS_QUERY = "SELECT * FROM $ABBREVS_TABLE"
    }
}
