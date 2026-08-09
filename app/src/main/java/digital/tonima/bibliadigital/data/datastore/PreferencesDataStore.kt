package digital.tonima.bibliadigital.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import digital.tonima.bibliadigital.domain.common.constants.STD_FONT_SIZE
import digital.tonima.bibliadigital.domain.core.exception.Failure
import digital.tonima.bibliadigital.domain.core.function.Either
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class PreferencesDataStore(private val context: Context) {
    private val fontSize = intPreferencesKey("font_size")
    private val showPressAndHoldVerseTutorial =
        booleanPreferencesKey("show_press_and_hold_verse_tutorial")

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
}

private val Context.preferencesDataStore: DataStore<Preferences> by preferencesDataStore("preferences")
