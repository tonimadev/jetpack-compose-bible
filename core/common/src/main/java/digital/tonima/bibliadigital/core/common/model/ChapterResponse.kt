package digital.tonima.bibliadigital.core.common.model

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import digital.tonima.bibliadigital.core.common.core.network.DomainModel
import digital.tonima.bibliadigital.core.common.model.DatabaseConstants.CHAPTERS_TABLE
import javax.inject.Singleton

@Immutable
@Singleton
@Entity(tableName = CHAPTERS_TABLE)
data class ChapterResponse(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @SerializedName("version")
    val version: String = "",
    @SerializedName("book")
    val book: Book = Book(),
    @SerializedName("chapter")
    val chapter: Chapter = Chapter(),
    @SerializedName("verses")
    val verses: List<Verse> = emptyList(),
) : DomainModel<BibleChapter> {
    override fun toDomain() =
        BibleChapter(
            version = version,
            book = book,
            chapter = chapter,
            verses = verses,
        )
}
