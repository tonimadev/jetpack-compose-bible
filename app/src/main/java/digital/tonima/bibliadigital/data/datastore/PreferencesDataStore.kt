package digital.tonima.bibliadigital.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import digital.tonima.bibliadigital.domain.common.constants.STD_FONT_SIZE
import digital.tonima.bibliadigital.domain.core.computation.Persistence
import digital.tonima.bibliadigital.domain.core.exception.Failure
import digital.tonima.bibliadigital.domain.core.function.Either
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class PreferencesDataStore(private val context: Context) : Persistence {
    private val fontSize = intPreferencesKey("font_size")
    private val showPressAndHoldVerseTutorial =
        booleanPreferencesKey("show_press_and_hold_verse_tutorial")
    private val selectedVersion = stringPreferencesKey("selected_version")
    private val lastReadBookName = stringPreferencesKey("last_read_book_name")
    private val lastReadBookAbbrev = stringPreferencesKey("last_read_book_abbrev")
    private val lastReadChapterId = intPreferencesKey("last_read_chapter_id")
    private val lastReadChapterQuantity = intPreferencesKey("last_read_chapter_quantity")
    private val dailyVerseDate = stringPreferencesKey("daily_verse_date")

    suspend fun storeFontSize(data: Int): Either<Failure, Unit> {
        context.preferencesDataStore.edit { preferences ->
            preferences[fontSize] = data
        }
        return Either.Success(Unit)
    }

    suspend fun readFontSize(): Either<Failure, Int> {
        return Either.Success(
            context.preferencesDataStore.data.map { preferences ->
                preferences[fontSize] ?: STD_FONT_SIZE
            }.first(),
        )
    }

    suspend fun disableShowPressAndHoldVerseTutorial(): Either<Failure, Unit> {
        context.preferencesDataStore.edit { preferences ->
            preferences[showPressAndHoldVerseTutorial] = false
        }
        return Either.Success(Unit)
    }

    suspend fun readShowPressAndHoldVerseTutorial(): Either<Failure, Boolean> {
        return Either.Success(
            context.preferencesDataStore.data.map { preferences ->
                preferences[showPressAndHoldVerseTutorial] ?: true
            }.first(),
        )
    }

    suspend fun storeSelectedVersion(version: String): Either<Failure, Unit> {
        context.preferencesDataStore.edit { preferences ->
            preferences[selectedVersion] = version
        }
        return Either.Success(Unit)
    }

    suspend fun readSelectedVersion(): Either<Failure, String> {
        return Either.Success(
            context.preferencesDataStore.data.map { preferences ->
                preferences[selectedVersion] ?: "nvi"
            }.first(),
        )
    }

    suspend fun storeReadingHistory(
        bookName: String,
        bookAbbrev: String,
        chapterId: Int,
        chapterQuantity: Int,
    ): Either<Failure, Unit> {
        context.preferencesDataStore.edit { preferences ->
            preferences[lastReadBookName] = bookName
            preferences[lastReadBookAbbrev] = bookAbbrev
            preferences[lastReadChapterId] = chapterId
            preferences[lastReadChapterQuantity] = chapterQuantity
        }
        return Either.Success(Unit)
    }

    suspend fun readReadingHistory(): Either<Failure, ReadingHistory?> {
        val prefs = context.preferencesDataStore.data.first()
        val name = prefs[lastReadBookName]
        val abbrev = prefs[lastReadBookAbbrev]
        val chapter = prefs[lastReadChapterId]
        val quantity = prefs[lastReadChapterQuantity]

        val history =
            if (name != null && abbrev != null && chapter != null && quantity != null) {
                ReadingHistory(name, abbrev, chapter, quantity)
            } else {
                null
            }

        return Either.Success(history)
    }
}

data class ReadingHistory(
    val bookName: String,
    val bookAbbrev: String,
    val chapterId: Int,
    val chapterQuantity: Int,
)

private val Context.preferencesDataStore: DataStore<Preferences> by preferencesDataStore("preferences")
