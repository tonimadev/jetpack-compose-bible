package digital.tonima.bibliadigital.core.common.model

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import digital.tonima.bibliadigital.core.common.core.computation.Database
import digital.tonima.bibliadigital.core.common.model.DatabaseConstants.ABBREVS_TABLE
import digital.tonima.bibliadigital.core.common.model.DatabaseConstants.BOOKS_TABLE
import digital.tonima.bibliadigital.core.common.model.DatabaseConstants.CHAPTERS_TABLE
import digital.tonima.bibliadigital.core.common.model.DatabaseConstants.VERSIONS_TABLE

@Dao
interface ChurchDao : Database {
    @Query("SELECT * FROM chapters")
    suspend fun getAllChapters(): List<ChapterResponse>

    @Query("SELECT * FROM books")
    suspend fun getAllBooks(): List<BookResponse>

    @Query("SELECT * FROM versions")
    suspend fun getAllVersions(): List<Version>

    @Query("SELECT * FROM abbrevsRoom")
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
