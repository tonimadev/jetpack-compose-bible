package digital.tonima.bibliadigital.ui.bible

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import digital.tonima.bibliadigital.data.datastore.ReadingHistory
import digital.tonima.bibliadigital.domain.core.exception.Failure
import digital.tonima.bibliadigital.domain.model.BookResponse
import digital.tonima.bibliadigital.domain.model.ChapterResponse
import digital.tonima.bibliadigital.domain.model.FavoriteVerse
import digital.tonima.bibliadigital.domain.model.Verse
import digital.tonima.bibliadigital.domain.model.Version
import digital.tonima.bibliadigital.ui.UiEvent
import digital.tonima.bibliadigital.ui.UiIntent
import digital.tonima.bibliadigital.ui.UiState

@Immutable
data class BibleState(
    val books: List<BookResponse> = emptyList(),
    val filteredBooks: List<BookResponse>? = null,
    val versions: List<Version> = emptyList(),
    val selectedVersion: String = "nvi",
    val chapter: ChapterResponse? = null,
    val fontSize: TextUnit = 16.sp,
    val currentChapter: Int = 1,
    val currentText: String = "",
    val lastSearch: String = "",
    val isLoading: Boolean = false,
    val isSpeechEnabled: Boolean = false,
    val showTutorial: Boolean = true,
    val selectedVerse: Verse? = null,
    val failure: Failure? = null,
    val history: ReadingHistory? = null,
    val favorites: List<FavoriteVerse> = emptyList(),
) : UiState

sealed class BibleIntent : UiIntent {
    object LoadBooks : BibleIntent()

    data class SearchBook(val query: String) : BibleIntent()

    data class LoadChapter(val bookName: String, val bookAbbrev: String, val chapterId: Int) : BibleIntent()

    data class UpdateLastSearch(val query: String) : BibleIntent()

    object ClearFilteredBooks : BibleIntent()

    object NextChapter : BibleIntent()

    object PreviousChapter : BibleIntent()

    object IncreaseFontSize : BibleIntent()

    object DecreaseFontSize : BibleIntent()

    data class SetSelectedVerse(val verse: Verse) : BibleIntent()

    object ClearSelectedVerse : BibleIntent()

    object DisableTutorial : BibleIntent()

    object LoadVersions : BibleIntent()

    data class ChangeVersion(val version: String) : BibleIntent()

    object LoadHistory : BibleIntent()

    object LoadFavorites : BibleIntent()

    data class ToggleFavorite(val verse: Verse, val bookName: String, val chapter: Int) : BibleIntent()

    data class TextToSpeech(val context: android.content.Context, val text: String) : BibleIntent()

    object StopSpeech : BibleIntent()
}

sealed class BibleEvent : UiEvent {
    data class ShowError(val failure: Failure) : BibleEvent()
}
