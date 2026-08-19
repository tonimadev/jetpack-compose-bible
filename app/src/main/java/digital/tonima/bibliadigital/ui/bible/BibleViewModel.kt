package digital.tonima.bibliadigital.ui.bible

import android.content.Context
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import digital.tonima.bibliadigital.domain.common.constants.MAX_FONT_SIZE
import digital.tonima.bibliadigital.domain.common.constants.MIN_FONT_SIZE
import digital.tonima.bibliadigital.domain.core.computation.CapabilityRegistry
import digital.tonima.bibliadigital.domain.core.exception.Failure
import digital.tonima.bibliadigital.domain.model.Book
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
import digital.tonima.bibliadigital.ui.StateContainer
import digital.tonima.bibliadigital.ui.StateContainerImpl
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
        private val registry: CapabilityRegistry,
        @ApplicationContext private val context: Context,
    ) : ViewModel(), StateContainer<BibleState> by StateContainerImpl(BibleState()) {
        private fun dispatch(mutation: BibleMutation) {
            updateState { BibleReducer.reduce(it, mutation) }
        }

        init {
            onIntent(BibleIntent.LoadBooks)
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
                    dispatch(BibleMutation.SpeechActiveChanged(active))
                }
            }

            viewModelScope.launch {
                ttsManager.isPaused.collect { isPaused ->
                    dispatch(BibleMutation.SpeechPausedChanged(isPaused))
                }
            }
        }

        fun onIntent(intent: BibleIntent) {
            when (intent) {
                is BibleIntent.LoadBooks -> getBooks()
                is BibleIntent.SearchBook -> dispatch(BibleMutation.SearchUpdated(intent.query))
                is BibleIntent.LoadChapter -> getBookChapter(intent.bookName, intent.bookAbbrev, intent.chapterId)
                is BibleIntent.UpdateLastSearch -> dispatch(BibleMutation.SearchUpdated(intent.query))
                is BibleIntent.ClearFilteredBooks -> dispatch(BibleMutation.ClearFilteredBooks)
                is BibleIntent.NextChapter -> nextChapter()
                is BibleIntent.PreviousChapter -> previousChapter()
                is BibleIntent.IncreaseFontSize -> increaseFontSize()
                is BibleIntent.DecreaseFontSize -> decreaseFontSize()
                is BibleIntent.SetSelectedVerse -> dispatch(BibleMutation.VerseSelected(intent.verse))
                is BibleIntent.ClearSelectedVerse -> dispatch(BibleMutation.VerseSelected(null))
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
                is BibleIntent.DismissError -> dispatch(BibleMutation.ClearFailure)
            }
        }

        private fun getShowTutorialValue() {
            getStoreShowPressAndHoldVerseTutorial(
                UseCase.None(),
                registry,
                viewModelScope,
            ) { it.fold(::handleBackgroundFailure) { show -> dispatch(BibleMutation.TutorialStatus(show)) } }
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
            dispatch(BibleMutation.Loading)
            getBooksUseCase(
                UseCase.None(),
                registry,
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
            dispatch(BibleMutation.Navigation(chapterId))
            getChapterUseCase(
                GetChapterUseCase.Params(bookName, bookAbbrev, chapterId, uiState.value.selectedVersion),
                registry,
                viewModelScope,
            ) {
                it.fold(
                    ::handleFailure,
                ) { response -> handleFetchBookChapterSuccess(response, isAutoSpeech) }
            }
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
            dispatch(
                BibleMutation.SpeechStarted(
                    bookName = bookName,
                    abbrev = bookAbbrev,
                    chapterId = chapter,
                    quantity = quantity,
                ),
            )
        }

        private fun pauseSpeech() {
            ttsManager.pause()
            dispatch(BibleMutation.SpeechPausedChanged(true))
        }

        private fun resumeSpeech() {
            ttsManager.resume()
            dispatch(BibleMutation.SpeechPausedChanged(false))
        }

        private fun stopSpeech() {
            ttsManager.stop()
            dispatch(BibleMutation.SpeechStopped)
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
                registry,
                viewModelScope,
            ) {
                it.fold(
                    ::handleBackgroundFailure,
                ) { size -> dispatch(BibleMutation.FontSizeChanged(size.sp)) }
            }
        }

        private fun storeFontSize(size: Int) {
            storeFontSizeUseCase(
                StoreFontSizeUseCase.Params(size),
                registry,
                viewModelScope,
            ) {
                it.fold(
                    ::handleFailure,
                ) { Timber.i("----- Font size stored") }
            }
        }

        private fun handleFailure(failure: Failure) {
            dispatch(BibleMutation.FailureOccurred(failure))
        }

        private fun handleBackgroundFailure(failure: Failure) {
            Timber.e("Background task failed: $failure")
        }

        private fun handleFetchBookChapterSuccess(
            chapterResponse: ChapterResponse,
            isAutoSpeech: Boolean = false,
        ) {
            dispatch(BibleMutation.ChapterLoaded(chapterResponse))
            saveHistory(chapterResponse)

            if (isAutoSpeech) {
                handleFetchBookChapterSuccessSpeech(chapterResponse)
            }
        }

        private fun handleFetchBookChapterSuccessSpeech(chapterResponse: ChapterResponse) {
            val currentText = chapterResponse.verses.joinToString(" ") { it.text }
            textToSpeech(
                context,
                currentText,
                chapterResponse.book.name,
                chapterResponse.chapter.number,
            )
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
                registry,
                viewModelScope,
            ) { /* ignore result */ }
        }

        private fun loadHistory() {
            getReadingHistoryUseCase(
                UseCase.None(),
                registry,
                viewModelScope,
            ) {
                it.fold(::handleBackgroundFailure) { history ->
                    dispatch(BibleMutation.HistoryLoaded(history))
                }
            }
        }

        private fun loadFavorites() {
            getFavoritesUseCase(
                UseCase.None(),
                registry,
                viewModelScope,
            ) {
                it.fold(::handleBackgroundFailure) { favorites ->
                    dispatch(BibleMutation.FavoritesLoaded(favorites))
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
                registry,
                viewModelScope,
            ) {
                it.fold(::handleFailure) {
                    loadFavorites() // Reload list
                }
            }
        }

        private fun handleBooksFetchSuccess(bookResponse: List<Book>) {
            dispatch(BibleMutation.BooksLoaded(bookResponse))
        }

        private fun increaseFontSize() {
            val current = uiState.value.fontSize.value.toInt()
            if (current < MAX_FONT_SIZE) {
                val next = current + 1
                dispatch(BibleMutation.FontSizeChanged(next.sp))
                storeFontSize(next)
            }
        }

        private fun decreaseFontSize() {
            val current = uiState.value.fontSize.value.toInt()
            if (current > MIN_FONT_SIZE) {
                val next = current - 1
                dispatch(BibleMutation.FontSizeChanged(next.sp))
                storeFontSize(next)
            }
        }

        private fun disableTutorials() {
            disableShowPressAndHoldVerseTutorialUseCase(
                UseCase.None(),
                registry,
                viewModelScope,
            ) {
                it.fold(
                    ::handleFailure,
                ) { dispatch(BibleMutation.TutorialStatus(false)) }
            }
        }

        private fun getVersions() {
            getVersionsUseCase(
                UseCase.None(),
                registry,
                viewModelScope,
            ) {
                it.fold(
                    ::handleBackgroundFailure,
                ) { versions -> dispatch(BibleMutation.VersionsLoaded(versions)) }
            }
        }

        private fun getSelectedVersion() {
            getSelectedVersionUseCase(
                UseCase.None(),
                registry,
                viewModelScope,
            ) {
                it.fold(
                    ::handleBackgroundFailure,
                ) { version -> dispatch(BibleMutation.SelectedVersionChanged(version)) }
            }
        }

        private fun changeVersion(version: String) {
            dispatch(BibleMutation.SelectedVersionChanged(version))
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
                registry,
                viewModelScope,
            ) {
                it.fold(
                    ::handleFailure,
                ) { Timber.i("----- Selected version stored") }
            }
        }
    }
