package digital.tonima.bibliadigital.ui.bible

import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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
    ) : BaseViewModel() {
        private var bruteFontSize = 16

        private val _showTutorial = MutableLiveData(true)
        private val _selectedVerse = MutableLiveData<Verse?>(null)
        private val _fontSize = MutableLiveData(bruteFontSize.sp)
        private val _currentChapter = MutableLiveData<Int>()
        private val _currentText = MutableLiveData<String>()
        private val _books: MutableLiveData<List<BookResponse>> = MutableLiveData()
        private val _lastSearch: MutableLiveData<String> = MutableLiveData("")
        private val _filteredBooks: MutableLiveData<List<BookResponse>?> = MutableLiveData()
        private val _chapter: MutableLiveData<ChapterResponse> = MutableLiveData()
        private var textToSpeech: TextToSpeech? = null
        private val _isSpeechEnabled: MutableLiveData<Boolean> = MutableLiveData(false)

        val showTutorial: LiveData<Boolean> = _showTutorial
        val selectedVerse: LiveData<Verse?> = _selectedVerse
        val fontSize: LiveData<TextUnit> = _fontSize
        val books: LiveData<List<BookResponse>> = _books
        val lastSearch: LiveData<String> = _lastSearch
        val filteredBooks: LiveData<List<BookResponse>?> = _filteredBooks
        val chapter: LiveData<ChapterResponse> = _chapter
        val isSpeechEnabled: LiveData<Boolean> = _isSpeechEnabled
        val currentText: LiveData<String> = _currentText
        val currentChapter: LiveData<Int> = _currentChapter

        init {
            getFontSize()
            getShowTutorialValue()
            getBooks()
        }

        private fun getShowTutorialValue() {
            return getStoreShowPressAndHoldVerseTutorial(
                UseCase.None(),
                viewModelScope,
            ) { it.fold(::handleFailure, ::handleShowTutorial) }
        }

        private fun handleShowTutorial(showTutorial: Boolean) {
            _showTutorial.value = showTutorial
        }

        fun setCurrentChapter(chapter: Int) {
            _currentChapter.value = chapter
        }

        fun nextChapter() {
            _currentChapter.value?.let {
                _currentChapter.value = it + 1
            }
        }

        fun previousChapter() {
            _currentChapter.value?.let {
                if (it > 1) _currentChapter.value = it - 1
            }
        }

        fun getBooks() {
            handleLoading(true)
            return getBooksUseCase(
                UseCase.None(),
                viewModelScope,
            ) {
                it.fold(
                    ::handleFailure,
                    ::handleBooksFetchSuccess,
                )
            }
        }

        fun getBookChapter(
            bookName: String,
            bookAbbrev: String,
            chapterId: Int,
        ) {
            handleLoading(true)
            _chapter.postValue(null)
            return getChapterUseCase(
                GetChapterUseCase.Params(bookName, bookAbbrev, chapterId),
                viewModelScope,
            ) {
                it.fold(
                    ::handleFailure,
                    ::handleFetchBookChapterSuccess,
                )
            }
        }

        fun searchBook(search: String) {
            _filteredBooks.postValue(
                _books.value?.filter {
                    it.name.removeAccents().contains(search, true) ||
                        it.abbrev.pt.contains(
                            search,
                            true,
                        )
                },
            )
        }

        fun textToSpeech(
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
            _isSpeechEnabled.postValue(true)
        }

        fun stopSpeech() {
            if (textToSpeech?.isSpeaking == true) {
                textToSpeech?.stop()
                textToSpeech?.shutdown()
            }
            _isSpeechEnabled.value = textToSpeech?.isSpeaking ?: false
        }

        private fun getFontSize() {
            getFontSizeUseCase(
                UseCase.None(),
                viewModelScope,
            ) {
                it.fold(
                    ::handleFailure,
                    ::handleFontSizeFetchSuccess,
                )
            }
        }

        private fun storeFontSize() {
            storeFontSizeUseCase(
                StoreFontSizeUseCase.Params(bruteFontSize),
                viewModelScope,
            ) {
                it.fold(
                    ::handleFailure,
                    ::handleFontSizeStoreSuccess,
                )
            }
        }

        private fun handleFontSizeStoreSuccess(fontSize: Unit) {
            Timber.i("----- Font size stored $fontSize")
        }

        private fun handleFontSizeFetchSuccess(fontSize: Int) {
            Timber.i("----- Font retrieved $fontSize")
            bruteFontSize = fontSize
            _fontSize.value = bruteFontSize.sp
        }

        private fun handleFetchBookChapterSuccess(chapterResponse: ChapterResponse) {
            handleLoading(false)
            _chapter.postValue(chapterResponse)
            _currentText.value =
                chapterResponse.verses.map { it.text + " " }
                    .toString()
        }

        private fun handleBooksFetchSuccess(bookResponse: List<BookResponse>) {
            handleLoading(false)
            _books.postValue(bookResponse)
        }

        private fun handleTutorialDisabled(unit: Unit) {
            _showTutorial.value = false
            Timber.i("----- Tutorial disabled $unit")
        }

        fun clearFilteredBooks() {
            _filteredBooks.postValue(null)
        }

        fun updateLastSearch(text: String) {
            _lastSearch.postValue(text)
        }

        fun increaseFontSize() {
            if (bruteFontSize < MAX_FONT_SIZE) {
                _fontSize.value = (++bruteFontSize).sp
                storeFontSize()
            }
        }

        fun decreaseFontSize() {
            if (bruteFontSize > MIN_FONT_SIZE) {
                _fontSize.value = (--bruteFontSize).sp
                storeFontSize()
            }
        }

        fun setSelectedVerse(verse: Verse) {
            _selectedVerse.value = verse
        }

        fun clearSelectedVerse() {
            _selectedVerse.value = null
        }

        fun disableTutorials() {
            return disableShowPressAndHoldVerseTutorialUseCase(
                UseCase.None(),
                viewModelScope,
            ) {
                it.fold(
                    ::handleFailure,
                    ::handleTutorialDisabled,
                )
            }
        }
    }
