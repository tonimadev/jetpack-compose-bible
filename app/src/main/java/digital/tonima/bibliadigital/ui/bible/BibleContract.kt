package digital.tonima.bibliadigital.ui.bible

import android.content.Context
import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import digital.tonima.bibliadigital.data.datastore.ReadingHistory
import digital.tonima.bibliadigital.domain.core.exception.Failure
import digital.tonima.bibliadigital.domain.core.extension.removeAccents
import digital.tonima.bibliadigital.domain.model.Book
import digital.tonima.bibliadigital.domain.model.ChapterResponse
import digital.tonima.bibliadigital.domain.model.FavoriteVerse
import digital.tonima.bibliadigital.domain.model.Verse
import digital.tonima.bibliadigital.domain.model.Version
import digital.tonima.bibliadigital.ui.UiEvent
import digital.tonima.bibliadigital.ui.UiIntent
import digital.tonima.bibliadigital.ui.UiState
import digital.tonima.bibliadigital.ui.bible.BibleMutation.BooksLoaded
import digital.tonima.bibliadigital.ui.bible.BibleMutation.ChapterLoaded
import digital.tonima.bibliadigital.ui.bible.BibleMutation.ClearFilteredBooks
import digital.tonima.bibliadigital.ui.bible.BibleMutation.FailureOccurred
import digital.tonima.bibliadigital.ui.bible.BibleMutation.FavoritesLoaded
import digital.tonima.bibliadigital.ui.bible.BibleMutation.FontSizeChanged
import digital.tonima.bibliadigital.ui.bible.BibleMutation.HistoryLoaded
import digital.tonima.bibliadigital.ui.bible.BibleMutation.Loading
import digital.tonima.bibliadigital.ui.bible.BibleMutation.Navigation
import digital.tonima.bibliadigital.ui.bible.BibleMutation.SearchUpdated
import digital.tonima.bibliadigital.ui.bible.BibleMutation.SelectedVersionChanged
import digital.tonima.bibliadigital.ui.bible.BibleMutation.SpeechActiveChanged
import digital.tonima.bibliadigital.ui.bible.BibleMutation.SpeechPausedChanged
import digital.tonima.bibliadigital.ui.bible.BibleMutation.SpeechStarted
import digital.tonima.bibliadigital.ui.bible.BibleMutation.SpeechStopped
import digital.tonima.bibliadigital.ui.bible.BibleMutation.TutorialStatus
import digital.tonima.bibliadigital.ui.bible.BibleMutation.VerseSelected
import digital.tonima.bibliadigital.ui.bible.BibleMutation.VersionsLoaded

@Immutable
data class BibleState(
    val books: List<Book> = emptyList(),
    val filteredBooks: List<Book>? = null,
    val versions: List<Version> = emptyList(),
    val selectedVersion: String = "nvi",
    val chapter: ChapterResponse? = null,
    val fontSize: TextUnit = 16.sp,
    val currentChapter: Int = 1,
    val currentText: String = "",
    val lastSearch: String = "",
    val isLoading: Boolean = false,
    val isSpeechEnabled: Boolean = false,
    val isSpeechPaused: Boolean = false,
    val playingBookName: String? = null,
    val playingBookAbbrev: String? = null,
    val playingChapterId: Int? = null,
    val playingChapterQuantity: Int? = null,
    val showTutorial: Boolean = true,
    val selectedVerse: Verse? = null,
    val failure: Failure? = null,
    val history: ReadingHistory? = null,
    val favorites: List<FavoriteVerse> = emptyList(),
) : UiState

sealed class BibleMutation {
    object Loading : BibleMutation()

    data class BooksLoaded(val books: List<Book>) : BibleMutation()

    data class ChapterLoaded(val chapter: ChapterResponse) : BibleMutation()

    data class SearchUpdated(val query: String) : BibleMutation()

    object ClearFilteredBooks : BibleMutation()

    data class FontSizeChanged(val fontSize: TextUnit) : BibleMutation()

    data class VerseSelected(val verse: Verse?) : BibleMutation()

    data class TutorialStatus(val show: Boolean) : BibleMutation()

    data class VersionsLoaded(val versions: List<Version>) : BibleMutation()

    data class SelectedVersionChanged(val version: String) : BibleMutation()

    data class HistoryLoaded(val history: ReadingHistory?) : BibleMutation()

    data class FavoritesLoaded(val favorites: List<FavoriteVerse>) : BibleMutation()

    data class FailureOccurred(val failure: Failure) : BibleMutation()

    data class SpeechActiveChanged(val isActive: Boolean) : BibleMutation()

    data class SpeechPausedChanged(val isPaused: Boolean) : BibleMutation()

    data class SpeechStarted(
        val bookName: String,
        val abbrev: String,
        val chapterId: Int,
        val quantity: Int,
    ) : BibleMutation()

    object SpeechStopped : BibleMutation()

    data class Navigation(val chapterId: Int) : BibleMutation()
}

object BibleReducer {
    fun reduce(
        state: BibleState,
        mutation: BibleMutation,
    ): BibleState {
        return when (mutation) {
            is Loading -> state.copy(isLoading = true, failure = null)
            is BooksLoaded -> state.copy(isLoading = false, books = mutation.books)
            is ChapterLoaded -> {
                val currentText = mutation.chapter.verses.joinToString(" ") { it.text }
                state.copy(
                    isLoading = false,
                    chapter = mutation.chapter,
                    currentText = currentText,
                )
            }
            is SearchUpdated -> {
                val filtered =
                    if (mutation.query.isEmpty()) {
                        null
                    } else {
                        state.books.filter {
                            it.name.removeAccents().contains(mutation.query, true) ||
                                it.abbrev.contains(mutation.query, true)
                        }
                    }
                state.copy(lastSearch = mutation.query, filteredBooks = filtered)
            }
            is ClearFilteredBooks -> state.copy(filteredBooks = null, lastSearch = "")
            is FontSizeChanged -> state.copy(fontSize = mutation.fontSize)
            is VerseSelected -> state.copy(selectedVerse = mutation.verse)
            is TutorialStatus -> state.copy(showTutorial = mutation.show)
            is VersionsLoaded -> state.copy(versions = mutation.versions)
            is SelectedVersionChanged -> state.copy(selectedVersion = mutation.version)
            is HistoryLoaded -> state.copy(history = mutation.history)
            is FavoritesLoaded -> state.copy(favorites = mutation.favorites)
            is FailureOccurred -> state.copy(isLoading = false, failure = mutation.failure)
            is SpeechActiveChanged ->
                state.copy(
                    isSpeechEnabled = mutation.isActive,
                    playingBookName = if (mutation.isActive) state.playingBookName else null,
                    playingBookAbbrev = if (mutation.isActive) state.playingBookAbbrev else null,
                    playingChapterId = if (mutation.isActive) state.playingChapterId else null,
                    playingChapterQuantity = if (mutation.isActive) state.playingChapterQuantity else null,
                )
            is SpeechPausedChanged -> state.copy(isSpeechPaused = mutation.isPaused)
            is SpeechStarted ->
                state.copy(
                    isSpeechEnabled = true,
                    isSpeechPaused = false,
                    playingBookName = mutation.bookName,
                    playingBookAbbrev = mutation.abbrev,
                    playingChapterId = mutation.chapterId,
                    playingChapterQuantity = mutation.quantity,
                )
            is SpeechStopped ->
                state.copy(
                    isSpeechEnabled = false,
                    isSpeechPaused = false,
                    playingBookName = null,
                    playingBookAbbrev = null,
                    playingChapterId = null,
                    playingChapterQuantity = null,
                )
            is Navigation -> state.copy(currentChapter = mutation.chapterId, chapter = null, isLoading = true)
        }
    }
}

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

    data class TextToSpeech(
        val context: Context,
        val text: String,
        val bookName: String,
        val chapter: Int,
    ) : BibleIntent()

    object StopSpeech : BibleIntent()

    object PauseSpeech : BibleIntent()

    object ResumeSpeech : BibleIntent()

    data class BindTTS(val context: Context) : BibleIntent()

    object UnbindTTS : BibleIntent()
}

sealed class BibleEvent : UiEvent {
    data class ShowError(val failure: Failure) : BibleEvent()
}
