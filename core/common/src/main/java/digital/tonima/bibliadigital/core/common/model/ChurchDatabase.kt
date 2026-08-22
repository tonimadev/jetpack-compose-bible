package digital.tonima.bibliadigital.core.common.model

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import digital.tonima.bibliadigital.core.common.model.DatabaseConstants.BIBLE_DB_VERSION
import javax.inject.Singleton

@Singleton
@Database(
    entities =
        [
            BookResponse::class,
            Verse::class,
            Chapter::class,
            ChapterResponse::class,
            Book::class,
            AbbrevRoomModel::class,
            FavoriteVerse::class,
            Version::class,
        ],
    version = BIBLE_DB_VERSION,
    exportSchema = false,
)
@TypeConverters(ChurchConverters::class)
abstract class ChurchDatabase : RoomDatabase() {
    abstract fun churchDao(): ChurchDao
}
