package digital.tonima.bibliadigital.core.common.model

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class ChurchConverters {
    private val gson = Gson()

    @TypeConverter
    fun abbrevToString(abbrev: Abbrev): String = gson.toJson(abbrev)

    @TypeConverter
    fun stringToAbbrev(abbrevString: String): Abbrev {
        return try {
            gson.fromJson(abbrevString, Abbrev::class.java) ?: Abbrev(pt = abbrevString)
        } catch (_: Exception) {
            Abbrev(pt = abbrevString)
        }
    }

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
