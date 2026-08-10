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

@Dao
interface ChurchDao {
    @Query(ALL_CHAPTERS_QUERY)
    fun getAllChapters(): List<ChapterResponse>

    @Query(ALL_BOOKS_QUERY)
    fun getAllBooks(): List<BookResponse>

    @Query(ALL_ABBREVS_QUERY)
    fun getAllAbbrevs(): List<AbbrevRoomModel>

    @Insert(onConflict = REPLACE)
    fun insertAllBooks(books: List<BookResponse>)

    @Insert(onConflict = REPLACE)
    fun insertAllChapters(chapters: List<ChapterResponse>)

    @Insert(onConflict = REPLACE)
    fun insertAllAbbrevs(abbrevs: List<AbbrevRoomModel>)

    companion object {
        const val ALL_CHAPTERS_QUERY = "SELECT * FROM $CHAPTERS_TABLE"
        const val ALL_BOOKS_QUERY = "SELECT * FROM $BOOKS_TABLE"
        const val ALL_ABBREVS_QUERY = "SELECT * FROM $ABBREVS_TABLE"
    }
}
