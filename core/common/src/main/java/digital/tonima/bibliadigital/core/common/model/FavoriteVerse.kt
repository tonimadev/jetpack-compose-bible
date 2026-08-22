package digital.tonima.bibliadigital.core.common.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import digital.tonima.bibliadigital.core.common.model.DatabaseConstants.FAVORITES_TABLE

@Entity(tableName = FAVORITES_TABLE)
data class FavoriteVerse(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val bookName: String,
    val chapter: Int,
    val verseNumber: Int,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
)
