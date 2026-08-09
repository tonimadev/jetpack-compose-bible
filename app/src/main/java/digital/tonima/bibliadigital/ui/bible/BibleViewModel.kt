package digital.tonima.bibliadigital.ui.bible

import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import digital.tonima.bibliadigital.domain.common.constants.MAX_FONT_SIZE
import digital.tonima.bibliadigital.domain.common.constants.MIN_FONT_SIZE
import digital.tonima.bibliadigital.domain.core.extension.removeAccents
import digital.tonima.bibliadigital.domain.model.BookResponse
import digital.tonima.bibliadigital.domain.model.ChapterResponse
import digital.tonima.bibliadigital.domain.model.Verse
import digital.tonima.bibliadigital.domain.usecases.DisableShowPressAndHoldVerseTutorialUseCase
import digital.tonima.bibliadigital.domain.usecases.GetBooksUseCase
import digital.tonima.bibliadigital.domain.usecases.GetChapterUseCase
import digital.tonima.bibliadigital.domain.usecases.GetFontSizeUseCase
import digital.tonima.bibliadigital.domain.usecases.GetShowPressAndHoldVerseTutorialUseCase
import digital.tonima.bibliadigital.domain.usecases.StoreFontSizeUseCase
import digital.tonima.bibliadigital.domain.usecases.UseCase
import digital.tonima.bibliadigital.ui.BaseViewModel
import timber.log.Timber
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class BibleViewModel
    @Inject
    constructor(
        private val getBooksUseCase: GetBooksUseCase,
        private val getChapterUseCase: GetChapterUseCase,
        private val getFontSizeUseCase: GetFontSizeUseCase,
        private val storeFontSizeUseCase: StoreFontSizeUseCase,
        private val disableShowPressAndHoldVerseTutorialUseCase: DisableShowPressAndHoldVerseTutorialUseCase,
        private val getStoreShowPressAndHoldVerseTutorial: GetShowPressAndHoldVerseTutorialUseCase,
    ) : BaseViewModel<BibleState, BibleIntent, BibleEvent>() {
        private var textToSpeech: TextToSpeech? = null

        override fun createInitialState() = BibleState()

        init {
            sendIntent(BibleIntent.LoadBooks)
            getFontSize()
            getShowTutorialValue()
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
                is BibleIntent.TextToSpeech -> textToSpeech(intent.context, intent.text)
                is BibleIntent.StopSpeech -> stopSpeech()
            }
        }

        private fun getShowTutorialValue() {
            getStoreShowPressAndHoldVerseTutorial(
                UseCase.None(),
                viewModelScope,
            ) { it.fold(::handleFailure, { show -> setState { copy(showTutorial = show) } }) }
        }

        private fun nextChapter() {
            val next = uiState.value.currentChapter + 1
            setState { copy(currentChapter = next) }
        }

        private fun previousChapter() {
            if (uiState.value.currentChapter > 1) {
                val prev = uiState.value.currentChapter - 1
                setState { copy(currentChapter = prev) }
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
            setState { copy(isLoading = true, chapter = null) }
            getChapterUseCase(
                GetChapterUseCase.Params(bookName, bookAbbrev, chapterId),
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
                        it.abbrev.pt.contains(search, true)
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
                    ::handleFailure,
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

        private fun handleFetchBookChapterSuccess(chapterResponse: ChapterResponse) {
            setState {
                copy(
                    isLoading = false,
                    chapter = chapterResponse,
                    currentText = chapterResponse.verses.joinToString(" ") { it.text },
                )
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
                    { setState { copy(showTutorial = false) } },
                )
            }
        }
    }
