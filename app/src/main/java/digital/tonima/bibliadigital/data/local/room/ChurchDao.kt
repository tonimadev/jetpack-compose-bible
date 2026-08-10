package digital.tonima.bibliadigital.data.local.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import digital.tonima.bibliadigital.data.local.room.ChurchDatabase.Companion.ABBREVS_TABLE
import digital.tonima.bibliadigital.data.local.room.ChurchDatabase.Companion.BOOKS_TABLE
import digital.tonima.bibliadigital.data.local.room.ChurchDatabase.Companion.CHAPTERS_TABLE
import digital.tonima.bibliadigital.domain.model.AbbrevRoomModel
import digital.tonima.bibliadigital.domain.model.BookResponse
import digital.tonima.bibliadigital.domain.model.ChapterResponse
import digital.tonima.bibliadigital.domain.model.FavoriteVerse

@Dao
interface ChurchDao {
    @Query(ALL_CHAPTERS_QUERY)
    fun getAllChapters(): List<ChapterResponse>

    @Query(ALL_BOOKS_QUERY)
    fun getAllBooks(): List<BookResponse>

    @Query(ALL_ABBREVS_QUERY)
    fun getAllAbbrevs(): List<AbbrevRoomModel>

    @Query("SELECT * FROM favorites ORDER BY timestamp DESC")
    fun getAllFavorites(): List<FavoriteVerse>

    @Insert(onConflict = REPLACE)
    fun insertAllBooks(books: List<BookResponse>)

    @Insert(onConflict = REPLACE)
    fun insertAllChapters(chapters: List<ChapterResponse>)

    @Insert(onConflict = REPLACE)
    fun insertAllAbbrevs(abbrevs: List<AbbrevRoomModel>)

    @Insert(onConflict = REPLACE)
    fun insertFavorite(favorite: FavoriteVerse)

    @Query("DELETE FROM favorites WHERE bookName = :bookName AND chapter = :chapter AND verseNumber = :verseNumber")
    fun deleteFavorite(
        bookName: String,
        chapter: Int,
        verseNumber: Int,
    )

    companion object {
        const val ALL_CHAPTERS_QUERY = "SELECT * FROM $CHAPTERS_TABLE"
        const val ALL_BOOKS_QUERY = "SELECT * FROM $BOOKS_TABLE"
        const val ALL_ABBREVS_QUERY = "SELECT * FROM $ABBREVS_TABLE"
    }
}
