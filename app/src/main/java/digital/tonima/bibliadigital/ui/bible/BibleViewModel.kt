package digital.tonima.bibliadigital.ui.bible

import android.content.Context
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
import digital.tonima.bibliadigital.ui.bible.tts.TTSEvent
import digital.tonima.bibliadigital.ui.bible.tts.TTSManager
import kotlinx.coroutines.launch
import timber.log.Timber
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
        private val ttsManager: TTSManager,
        @ApplicationContext private val context: Context,
    ) : BaseViewModel<BibleState, BibleIntent, BibleEvent>() {
        override fun createInitialState() = BibleState()

        init {
            sendIntent(BibleIntent.LoadBooks)
            getFontSize()
            getShowTutorialValue()
            getSelectedVersion()
            getVersions()
            loadHistory()
            loadFavorites()
            observeTTS()
        }

        private fun observeTTS() {
            viewModelScope.launch {
                ttsManager.events.collect { event ->
                    when (event) {
                        is TTSEvent.NextChapter -> nextChapter(isAutoSpeech = true)
                        is TTSEvent.PreviousChapter -> previousChapter(isAutoSpeech = true)
                    }
                }
            }

            viewModelScope.launch {
                ttsManager.isSessionActive.collect { active ->
                    setState {
                        copy(
                            isSpeechEnabled = active,
                            playingBookName = if (active) playingBookName else null,
                            playingBookAbbrev = if (active) playingBookAbbrev else null,
                            playingChapterId = if (active) playingChapterId else null,
                            playingChapterQuantity = if (active) playingChapterQuantity else null,
                        )
                    }
                }
            }

            viewModelScope.launch {
                ttsManager.isPaused.collect { isPaused ->
                    setState { copy(isSpeechPaused = isPaused) }
                }
            }
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
                is BibleIntent.TextToSpeech ->
                    textToSpeech(
                        intent.context,
                        intent.text,
                        intent.bookName,
                        intent.chapter,
                    )
                is BibleIntent.StopSpeech -> stopSpeech()
                is BibleIntent.PauseSpeech -> pauseSpeech()
                is BibleIntent.ResumeSpeech -> resumeSpeech()
                is BibleIntent.BindTTS -> bindTTS(intent.context)
                is BibleIntent.UnbindTTS -> unbindTTS()
            }
        }

        private fun getShowTutorialValue() {
            getStoreShowPressAndHoldVerseTutorial(
                UseCase.None(),
                viewModelScope,
            ) { it.fold(::handleBackgroundFailure) { show -> setState { copy(showTutorial = show) } } }
        }

        private fun nextChapter(isAutoSpeech: Boolean = false) {
            val next = uiState.value.currentChapter + 1
            val book = uiState.value.chapter?.book
            if (book != null) {
                getBookChapter(book.name, book.abbrev, next, isAutoSpeech)
            }
        }

        private fun previousChapter(isAutoSpeech: Boolean = false) {
            if (uiState.value.currentChapter > 1) {
                val prev = uiState.value.currentChapter - 1
                val book = uiState.value.chapter?.book
                if (book != null) {
                    getBookChapter(book.name, book.abbrev, prev, isAutoSpeech)
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
            isAutoSpeech: Boolean = false,
        ) {
            val isAlreadyPlayingThis =
                uiState.value.isSpeechEnabled &&
                    uiState.value.playingBookAbbrev == bookAbbrev &&
                    uiState.value.playingChapterId == chapterId

            if (!isAutoSpeech && !isAlreadyPlayingThis) {
                stopSpeech()
            }
            setState { copy(isLoading = true, chapter = null, currentChapter = chapterId) }
            getChapterUseCase(
                GetChapterUseCase.Params(bookName, bookAbbrev, chapterId, uiState.value.selectedVersion),
                viewModelScope,
            ) {
                it.fold(
                    ::handleFailure,
                    { response -> handleFetchBookChapterSuccess(response, isAutoSpeech) },
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
            bookName: String,
            chapter: Int,
        ) {
            if (text.isBlank()) {
                Timber.w("Speech requested but text is empty")
                return
            }

            // Find current book to get abbrev and total chapters if possible
            val currentChapter = uiState.value.chapter
            val bookAbbrev = currentChapter?.book?.abbrev ?: ""
            val quantity = uiState.value.books.find { it.abbrev == bookAbbrev }?.chapters ?: 50

            ttsManager.startSpeaking(context, text, bookName, chapter)
            setState {
                copy(
                    isSpeechEnabled = true,
                    isSpeechPaused = false,
                    playingBookName = bookName,
                    playingBookAbbrev = bookAbbrev,
                    playingChapterId = chapter,
                    playingChapterQuantity = quantity,
                )
            }
        }

        private fun pauseSpeech() {
            ttsManager.pause()
            setState { copy(isSpeechPaused = true) }
        }

        private fun resumeSpeech() {
            ttsManager.resume()
            setState { copy(isSpeechPaused = false) }
        }

        private fun stopSpeech() {
            ttsManager.stop()
            setState {
                copy(
                    isSpeechEnabled = false,
                    isSpeechPaused = false,
                    playingBookName = null,
                    playingBookAbbrev = null,
                    playingChapterId = null,
                    playingChapterQuantity = null,
                )
            }
        }

        private fun bindTTS(context: Context) {
            ttsManager.bind(context)
        }

        private fun unbindTTS() {
            ttsManager.unbind()
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

        private fun handleFetchBookChapterSuccess(
            chapterResponse: ChapterResponse,
            isAutoSpeech: Boolean = false,
        ) {
            val currentText = chapterResponse.verses.joinToString(" ") { it.text }
            setState {
                copy(
                    isLoading = false,
                    chapter = chapterResponse,
                    currentText = currentText,
                )
            }
            saveHistory(chapterResponse)

            if (isAutoSpeech) {
                textToSpeech(
                    context,
                    currentText,
                    chapterResponse.book.name,
                    chapterResponse.chapter.number,
                )
            }
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
