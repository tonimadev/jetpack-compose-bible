package digital.tonima.bibliadigital.data.local.room

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import digital.tonima.bibliadigital.domain.model.Abbrev
import digital.tonima.bibliadigital.domain.model.Book
import digital.tonima.bibliadigital.domain.model.Chapter
import digital.tonima.bibliadigital.domain.model.Verse
import java.lang.reflect.Type

class ChurchConverters {
    private val gson = Gson()

    @TypeConverter
    fun abbrevToString(abbrev: Abbrev): String = gson.toJson(abbrev)

    @TypeConverter
    fun stringToAbbrev(abbrevString: String): Abbrev = gson.fromJson(abbrevString, Abbrev::class.java)

    @TypeConverter
    fun bookToString(book: Book): String = gson.toJson(book)

    @TypeConverter
    fun stringToBook(bookString: String): Book = gson.fromJson(bookString, Book::class.java)

    @TypeConverter
    fun chapterToString(chapter: Chapter): String = gson.toJson(chapter)

    @TypeConverter
    fun stringToChapter(chapterString: String): Chapter = gson.fromJson(chapterString, Chapter::class.java)

    @TypeConverter
    fun verseListToString(verseList: List<Verse>): String = gson.toJson(verseList)

    @TypeConverter
    fun stringToVerseList(verseListString: String): List<Verse> {
        val type: Type = object : TypeToken<ArrayList<Verse>>() {}.type

        return gson.fromJson(verseListString, type)
    }
}
