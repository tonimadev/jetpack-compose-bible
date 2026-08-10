package digital.tonima.bibliadigital.ui.bible

import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import digital.tonima.bibliadigital.domain.common.constants.MAX_FONT_SIZE
import digital.tonima.bibliadigital.domain.common.constants.MIN_FONT_SIZE
import digital.tonima.bibliadigital.domain.core.exception.Failure
import digital.tonima.bibliadigital.domain.core.extension.removeAccents
import digital.tonima.bibliadigital.domain.model.BookResponse
import digital.tonima.bibliadigital.domain.model.ChapterResponse
import digital.tonima.bibliadigital.domain.model.Verse
import digital.tonima.bibliadigital.domain.usecases.DisableShowPressAndHoldVerseTutorialUseCase
import digital.tonima.bibliadigital.domain.usecases.GetBooksUseCase
import digital.tonima.bibliadigital.domain.usecases.GetChapterUseCase
import digital.tonima.bibliadigital.domain.usecases.GetFavoritesUseCase
import digital.tonima.bibliadigital.domain.usecases.GetFontSizeUseCase
import digital.tonima.bibliadigital.domain.usecases.GetReadingHistoryUseCase
import digital.tonima.bibliadigital.domain.usecases.GetSelectedVersionUseCase
import digital.tonima.bibliadigital.domain.usecases.GetShowPressAndHoldVerseTutorialUseCase
import digital.tonima.bibliadigital.domain.usecases.GetVersionsUseCase
import digital.tonima.bibliadigital.domain.usecases.StoreFontSizeUseCase
import digital.tonima.bibliadigital.domain.usecases.StoreReadingHistoryUseCase
import digital.tonima.bibliadigital.domain.usecases.StoreSelectedVersionUseCase
import digital.tonima.bibliadigital.domain.usecases.ToggleFavoriteUseCase
import digital.tonima.bibliadigital.domain.usecases.UseCase
import digital.tonima.bibliadigital.ui.BaseViewModel
import timber.log.Timber
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class BibleViewModel
    @Inject
    @Suppress("LongParameterList")
    constructor(
        private val getBooksUseCase: GetBooksUseCase,
        private val getChapterUseCase: GetChapterUseCase,
        private val getFontSizeUseCase: GetFontSizeUseCase,
        private val storeFontSizeUseCase: StoreFontSizeUseCase,
        private val disableShowPressAndHoldVerseTutorialUseCase: DisableShowPressAndHoldVerseTutorialUseCase,
        private val getStoreShowPressAndHoldVerseTutorial: GetShowPressAndHoldVerseTutorialUseCase,
        private val getVersionsUseCase: GetVersionsUseCase,
        private val getSelectedVersionUseCase: GetSelectedVersionUseCase,
        private val storeSelectedVersionUseCase: StoreSelectedVersionUseCase,
        private val storeReadingHistoryUseCase: StoreReadingHistoryUseCase,
        private val getReadingHistoryUseCase: GetReadingHistoryUseCase,
        private val getFavoritesUseCase: GetFavoritesUseCase,
        private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    ) : BaseViewModel<BibleState, BibleIntent, BibleEvent>() {
        private var textToSpeech: TextToSpeech? = null

        override fun createInitialState() = BibleState()

        init {
            sendIntent(BibleIntent.LoadBooks)
            getFontSize()
            getShowTutorialValue()
            getSelectedVersion()
            getVersions()
            loadHistory()
            loadFavorites()
        }

        override fun handleIntent(intent: BibleIntent) {
            when (intent) {
                is BibleIntent.LoadBooks -> getBooks()
                is BibleIntent.SearchBook -> searchBook(intent.query)
                is BibleIntent.LoadChapter -> getBookChapter(intent.bookName, intent.bookAbbrev, intent.chapterId)
                is BibleIntent.UpdateLastSearch -> updateLastSearch(intent.query)
                is BibleIntent.ClearFilteredBooks -> clearFilteredBooks()
                is BibleIntent.NextChapter -> nextChapter()
                is BibleIntent.PreviousChapter -> previousChapter()
                is BibleIntent.IncreaseFontSize -> increaseFontSize()
                is BibleIntent.DecreaseFontSize -> decreaseFontSize()
                is BibleIntent.SetSelectedVerse -> setSelectedVerse(intent.verse)
                is BibleIntent.ClearSelectedVerse -> clearSelectedVerse()
                is BibleIntent.DisableTutorial -> disableTutorials()
                is BibleIntent.LoadVersions -> getVersions()
                is BibleIntent.ChangeVersion -> changeVersion(intent.version)
                is BibleIntent.LoadHistory -> loadHistory()
                is BibleIntent.LoadFavorites -> loadFavorites()
                is BibleIntent.ToggleFavorite -> toggleFavorite(intent.verse, intent.bookName, intent.chapter)
                is BibleIntent.TextToSpeech -> textToSpeech(intent.context, intent.text)
                is BibleIntent.StopSpeech -> stopSpeech()
            }
        }

        private fun getShowTutorialValue() {
            getStoreShowPressAndHoldVerseTutorial(
                UseCase.None(),
                viewModelScope,
            ) { it.fold(::handleBackgroundFailure) { show -> setState { copy(showTutorial = show) } } }
        }

        private fun nextChapter() {
            val next = uiState.value.currentChapter + 1
            val book = uiState.value.chapter?.book
            if (book != null) {
                getBookChapter(book.name, book.abbrev, next)
            }
        }

        private fun previousChapter() {
            if (uiState.value.currentChapter > 1) {
                val prev = uiState.value.currentChapter - 1
                val book = uiState.value.chapter?.book
                if (book != null) {
                    getBookChapter(book.name, book.abbrev, prev)
                }
            }
        }

        private fun getBooks() {
            setState { copy(isLoading = true) }
            getBooksUseCase(
                UseCase.None(),
                viewModelScope,
            ) {
                it.fold(
                    ::handleFailure,
                    ::handleBooksFetchSuccess,
                )
            }
        }

        private fun getBookChapter(
            bookName: String,
            bookAbbrev: String,
            chapterId: Int,
        ) {
            stopSpeech()
            setState { copy(isLoading = true, chapter = null, currentChapter = chapterId) }
            getChapterUseCase(
                GetChapterUseCase.Params(bookName, bookAbbrev, chapterId, uiState.value.selectedVersion),
                viewModelScope,
            ) {
                it.fold(
                    ::handleFailure,
                    ::handleFetchBookChapterSuccess,
                )
            }
        }

        private fun searchBook(search: String) {
            val filtered =
                uiState.value.books.filter {
                    it.name.removeAccents().contains(search, true) ||
                        it.abbrev.contains(search, true)
                }
            setState { copy(filteredBooks = filtered) }
        }

        private fun textToSpeech(
            context: Context,
            text: String,
        ) {
            textToSpeech =
                TextToSpeech(
                    context,
                ) {
                    if (it == TextToSpeech.SUCCESS) {
                        textToSpeech?.let { txtToSpeech ->
                            txtToSpeech.language = Locale.getDefault()
                            txtToSpeech.setSpeechRate(1f)
                            txtToSpeech.speak(
                                text,
                                TextToSpeech.QUEUE_FLUSH,
                                null,
                                null,
                            )
                        }
                    }
                }
            setState { copy(isSpeechEnabled = true) }
        }

        private fun stopSpeech() {
            if (textToSpeech?.isSpeaking == true) {
                textToSpeech?.stop()
                textToSpeech?.shutdown()
            }
            setState { copy(isSpeechEnabled = textToSpeech?.isSpeaking ?: false) }
        }

        private fun getFontSize() {
            getFontSizeUseCase(
                UseCase.None(),
                viewModelScope,
            ) {
                it.fold(
                    ::handleBackgroundFailure,
                    { size -> setState { copy(fontSize = size.sp) } },
                )
            }
        }

        private fun storeFontSize(size: Int) {
            storeFontSizeUseCase(
                StoreFontSizeUseCase.Params(size),
                viewModelScope,
            ) {
                it.fold(
                    ::handleFailure,
                    { Timber.i("----- Font size stored") },
                )
            }
        }

        override fun handleFailure(failure: Failure) {
            setState { copy(isLoading = false, failure = failure) }
        }

        private fun handleBackgroundFailure(failure: Failure) {
            Timber.e("Background task failed: $failure")
        }

        private fun handleFetchBookChapterSuccess(chapterResponse: ChapterResponse) {
            setState {
                copy(
                    isLoading = false,
                    chapter = chapterResponse,
                    currentText = chapterResponse.verses.joinToString(" ") { it.text },
                )
            }
            saveHistory(chapterResponse)
        }

        private fun saveHistory(chapterResponse: ChapterResponse) {
            // Find book to get total chapters (needed for history navigation)
            val book = uiState.value.books.find { it.abbrev == chapterResponse.book.abbrev }
            val quantity = book?.chapters ?: 50 // fallback

            storeReadingHistoryUseCase(
                StoreReadingHistoryUseCase.Params(
                    bookName = chapterResponse.book.name,
                    bookAbbrev = chapterResponse.book.abbrev,
                    chapterId = chapterResponse.chapter.number,
                    chapterQuantity = quantity,
                ),
                viewModelScope,
            ) { /* ignore result */ }
        }

        private fun loadHistory() {
            getReadingHistoryUseCase(
                UseCase.None(),
                viewModelScope,
            ) {
                it.fold(::handleBackgroundFailure) { history ->
                    setState { copy(history = history) }
                }
            }
        }

        private fun loadFavorites() {
            getFavoritesUseCase(
                UseCase.None(),
                viewModelScope,
            ) {
                it.fold(::handleBackgroundFailure) { favorites ->
                    setState { copy(favorites = favorites) }
                }
            }
        }

        private fun toggleFavorite(
            verse: Verse,
            bookName: String,
            chapter: Int,
        ) {
            toggleFavoriteUseCase(
                ToggleFavoriteUseCase.Params(bookName, chapter, verse.number, verse.text),
                viewModelScope,
            ) {
                it.fold(::handleFailure) {
                    loadFavorites() // Reload list
                }
            }
        }

        private fun handleBooksFetchSuccess(bookResponse: List<BookResponse>) {
            setState { copy(isLoading = false, books = bookResponse) }
        }

        private fun clearFilteredBooks() {
            setState { copy(filteredBooks = null) }
        }

        private fun updateLastSearch(text: String) {
            setState { copy(lastSearch = text) }
        }

        private fun increaseFontSize() {
            val current = uiState.value.fontSize.value.toInt()
            if (current < MAX_FONT_SIZE) {
                val next = current + 1
                setState { copy(fontSize = next.sp) }
                storeFontSize(next)
            }
        }

        private fun decreaseFontSize() {
            val current = uiState.value.fontSize.value.toInt()
            if (current > MIN_FONT_SIZE) {
                val next = current - 1
                setState { copy(fontSize = next.sp) }
                storeFontSize(next)
            }
        }

        private fun setSelectedVerse(verse: Verse) {
            setState { copy(selectedVerse = verse) }
        }

        private fun clearSelectedVerse() {
            setState { copy(selectedVerse = null) }
        }

        private fun disableTutorials() {
            disableShowPressAndHoldVerseTutorialUseCase(
                UseCase.None(),
                viewModelScope,
            ) {
                it.fold(
                    ::handleFailure,
                ) { setState { copy(showTutorial = false) } }
            }
        }

        private fun getVersions() {
            getVersionsUseCase(
                UseCase.None(),
                viewModelScope,
            ) {
                it.fold(
                    ::handleBackgroundFailure,
                    { versions -> setState { copy(versions = versions) } },
                )
            }
        }

        private fun getSelectedVersion() {
            getSelectedVersionUseCase(
                UseCase.None(),
                viewModelScope,
            ) {
                it.fold(
                    ::handleBackgroundFailure,
                    { version -> setState { copy(selectedVersion = version) } },
                )
            }
        }

        private fun changeVersion(version: String) {
            setState { copy(selectedVersion = version) }
            storeSelectedVersion(version)
            val currentChapter = uiState.value.chapter
            if (currentChapter != null) {
                getBookChapter(
                    currentChapter.book.name,
                    currentChapter.book.abbrev,
                    currentChapter.chapter.number,
                )
            }
        }

        private fun storeSelectedVersion(version: String) {
            storeSelectedVersionUseCase(
                StoreSelectedVersionUseCase.Params(version),
                viewModelScope,
            ) {
                it.fold(
                    ::handleFailure,
                    { Timber.i("----- Selected version stored") },
                )
            }
        }
    }
