package digital.tonima.bibliadigital.data.local.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import digital.tonima.bibliadigital.data.local.room.ChurchDatabase.Companion.BIBLE_DB_VERSION
import digital.tonima.bibliadigital.domain.model.Abbrev
import digital.tonima.bibliadigital.domain.model.AbbrevRoomModel
import digital.tonima.bibliadigital.domain.model.Book
import digital.tonima.bibliadigital.domain.model.BookResponse
import digital.tonima.bibliadigital.domain.model.Chapter
import digital.tonima.bibliadigital.domain.model.ChapterResponse
import digital.tonima.bibliadigital.domain.model.Verse
import javax.inject.Singleton

@Singleton
@Database(
    entities =
        [
            BookResponse::class,
            Abbrev::class,
            Verse::class,
            Chapter::class,
            ChapterResponse::class,
            Book::class,
            AbbrevRoomModel::class,
        ],
    version = BIBLE_DB_VERSION,
    exportSchema = false,
)
@TypeConverters(ChurchConverters::class)
abstract class ChurchDatabase : RoomDatabase() {
    abstract fun churchDao(): ChurchDao

    companion object {
        const val BIBLE_DB_VERSION = 18
        const val BIBLE_DB_NAME = "bible"
        const val BOOKS_TABLE = "books"
        const val CHAPTERS_TABLE = "chapters"
        const val ABBREVS_TABLE = "abbrevsRoom"
    }
}
