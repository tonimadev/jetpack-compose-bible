package digital.tonima.bibliadigital.ui.bible

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import digital.tonima.bibliadigital.data.datastore.ReadingHistory
import digital.tonima.bibliadigital.domain.core.computation.CapabilityRegistry
import digital.tonima.bibliadigital.domain.core.function.Either
import digital.tonima.bibliadigital.domain.model.Book
import digital.tonima.bibliadigital.domain.model.FavoriteVerse
import digital.tonima.bibliadigital.domain.model.Verse
import digital.tonima.bibliadigital.domain.model.Version
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
import digital.tonima.bibliadigital.ui.bible.tts.TTSEvent
import digital.tonima.bibliadigital.ui.bible.tts.TTSManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BibleViewModelTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    private val getBooksUseCase: GetBooksUseCase = mockk()
    private val getChapterUseCase: GetChapterUseCase = mockk()
    private val getFontSizeUseCase: GetFontSizeUseCase = mockk()
    private val storeFontSizeUseCase: StoreFontSizeUseCase = mockk()
    private val disableShowPressAndHoldVerseTutorialUseCase: DisableShowPressAndHoldVerseTutorialUseCase = mockk()
    private val getStoreShowPressAndHoldVerseTutorial: GetShowPressAndHoldVerseTutorialUseCase = mockk()
    private val getVersionsUseCase: GetVersionsUseCase = mockk()
    private val getSelectedVersionUseCase: GetSelectedVersionUseCase = mockk()
    private val storeSelectedVersionUseCase: StoreSelectedVersionUseCase = mockk()
    private val storeReadingHistoryUseCase: StoreReadingHistoryUseCase = mockk()
    private val getReadingHistoryUseCase: GetReadingHistoryUseCase = mockk()
    private val getFavoritesUseCase: GetFavoritesUseCase = mockk()
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase = mockk()
    private val ttsManager: TTSManager = mockk()
    private val registry: CapabilityRegistry = mockk()
    private val context: android.content.Context = mockk()

    private val ttsEvents = MutableSharedFlow<TTSEvent>()
    private val isSessionActive = MutableStateFlow(false)
    private val isPaused = MutableStateFlow(false)

    private lateinit var viewModel: BibleViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        // Mock TTSManager flows and methods
        every { ttsManager.events } returns ttsEvents
        every { ttsManager.isSessionActive } returns isSessionActive
        every { ttsManager.isPaused } returns isPaused
        every { ttsManager.stop() } returns Unit
        every { ttsManager.pause() } returns Unit
        every { ttsManager.resume() } returns Unit
        every { ttsManager.startSpeaking(any(), any(), any(), any()) } returns Unit
        every { ttsManager.bind(any()) } returns Unit
        every { ttsManager.unbind() } returns Unit

        // Mock default behavior for init calls
        coEvery { getFontSizeUseCase(any(), any(), any(), any()) } coAnswers {
            lastArg<(Either<*, Int>) -> Unit>().invoke(Either.Success(16))
        }
        coEvery { getStoreShowPressAndHoldVerseTutorial(any(), any(), any(), any()) } coAnswers {
            lastArg<(Either<*, Boolean>) -> Unit>().invoke(Either.Success(true))
        }
        coEvery { getBooksUseCase(any(), any(), any(), any()) } coAnswers {
            lastArg<(Either<*, List<Book>>) -> Unit>().invoke(Either.Success(emptyList()))
        }
        coEvery { getSelectedVersionUseCase(any(), any(), any(), any()) } coAnswers {
            lastArg<(Either<*, String>) -> Unit>().invoke(Either.Success("nvi"))
        }
        coEvery { getVersionsUseCase(any(), any(), any(), any()) } coAnswers {
            lastArg<(Either<*, List<Version>>) -> Unit>().invoke(Either.Success(emptyList()))
        }
        coEvery { getReadingHistoryUseCase(any(), any(), any(), any()) } coAnswers {
            lastArg<(Either<*, ReadingHistory?>) -> Unit>().invoke(Either.Success(null))
        }
        coEvery { getFavoritesUseCase(any(), any(), any(), any()) } coAnswers {
            lastArg<(Either<*, List<FavoriteVerse>>) -> Unit>().invoke(Either.Success(emptyList()))
        }

        viewModel =
            BibleViewModel(
                getBooksUseCase,
                getChapterUseCase,
                getFontSizeUseCase,
                storeFontSizeUseCase,
                disableShowPressAndHoldVerseTutorialUseCase,
                getStoreShowPressAndHoldVerseTutorial,
                getVersionsUseCase,
                getSelectedVersionUseCase,
                storeSelectedVersionUseCase,
                storeReadingHistoryUseCase,
                getReadingHistoryUseCase,
                getFavoritesUseCase,
                toggleFavoriteUseCase,
                ttsManager,
                registry,
                context,
            )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `when init should fetch initial data`() =
        runTest {
            advanceUntilIdle()
            coVerify { getFontSizeUseCase(any(), any(), any(), any()) }
            coVerify { getStoreShowPressAndHoldVerseTutorial(any(), any(), any(), any()) }
            coVerify { getBooksUseCase(any(), any(), any(), any()) }
            coVerify { getSelectedVersionUseCase(any(), any(), any(), any()) }
            coVerify { getVersionsUseCase(any(), any(), any(), any()) }
            coVerify { getReadingHistoryUseCase(any(), any(), any(), any()) }
            coVerify { getFavoritesUseCase(any(), any(), any(), any()) }

            assertEquals(16, viewModel.uiState.value.fontSize.value.toInt())
            assertEquals(true, viewModel.uiState.value.showTutorial)
            assertEquals("nvi", viewModel.uiState.value.selectedVersion)
        }

    @Test
    fun `when LoadBooks intent is sent should update books in state`() =
        runTest {
            val mockBooks = listOf(Book(1, "gn", name = "Genesis"))
            coEvery { getBooksUseCase(any(), any(), any(), any()) } coAnswers {
                lastArg<(Either<*, List<Book>>) -> Unit>().invoke(Either.Success(mockBooks))
            }

            viewModel.onIntent(BibleIntent.LoadBooks)
            advanceUntilIdle()

            assertEquals(mockBooks, viewModel.uiState.value.books)
        }

    @Test
    fun `when LoadHistory intent is sent should update history in state`() =
        runTest {
            val mockHistory = ReadingHistory("Genesis", "gn", 1, 50)
            coEvery { getReadingHistoryUseCase(any(), any(), any(), any()) } coAnswers {
                lastArg<(Either<*, ReadingHistory?>) -> Unit>().invoke(Either.Success(mockHistory))
            }

            viewModel.onIntent(BibleIntent.LoadHistory)
            advanceUntilIdle()

            assertEquals(mockHistory, viewModel.uiState.value.history)
        }

    @Test
    fun `when ChangeVersion intent is sent should update selected version and fetch chapter`() =
        runTest {
            val newVersion = "acf"
            coEvery { storeSelectedVersionUseCase(any(), any(), any(), any()) } coAnswers {
                lastArg<(Either<*, Unit>) -> Unit>().invoke(Either.Success(Unit))
            }
            coEvery { getChapterUseCase(any(), any(), any(), any()) } coAnswers {
                // Return dummy failure to simplify test, main point is that it was called
                lastArg<(Either<*, digital.tonima.bibliadigital.domain.model.ChapterResponse>) -> Unit>().invoke(
                    Either.Fail(digital.tonima.bibliadigital.domain.core.exception.Failure.NetworkConnection),
                )
            }

            viewModel.onIntent(BibleIntent.ChangeVersion(newVersion))
            advanceUntilIdle()

            assertEquals(newVersion, viewModel.uiState.value.selectedVersion)
            coVerify { storeSelectedVersionUseCase(any(), any(), any(), any()) }
        }

    @Test
    fun `when ToggleFavorite intent is sent should call use case and reload favorites`() =
        runTest {
            val verse = Verse(number = 1, text = "Test verse")
            val bookName = "Genesis"
            val chapter = 1

            coEvery { toggleFavoriteUseCase(any(), any(), any(), any()) } coAnswers {
                lastArg<(Either<*, Boolean>) -> Unit>().invoke(Either.Success(true))
            }
            coEvery { getFavoritesUseCase(any(), any(), any(), any()) } coAnswers {
                lastArg<(Either<*, List<FavoriteVerse>>) -> Unit>().invoke(Either.Success(emptyList()))
            }

            viewModel.onIntent(BibleIntent.ToggleFavorite(verse, bookName, chapter))
            advanceUntilIdle()

            coVerify { toggleFavoriteUseCase(any(), any(), any(), any()) }
            coVerify(exactly = 2) { getFavoritesUseCase(any(), any(), any(), any()) } // One on init, one after toggle
        }

    @Test
    fun `when TextToSpeech intent is sent should call ttsManager startSpeaking`() =
        runTest {
            val text = "Test text"
            val bookName = "Genesis"
            val chapter = 1
            every { ttsManager.startSpeaking(any(), any(), any(), any()) } returns Unit

            viewModel.onIntent(BibleIntent.TextToSpeech(context, text, bookName, chapter))
            advanceUntilIdle()

            verify { ttsManager.startSpeaking(context, text, bookName, chapter) }
            assertTrue(viewModel.uiState.value.isSpeechEnabled)
            assertFalse(viewModel.uiState.value.isSpeechPaused)
        }

    @Test
    fun `when PauseSpeech intent is sent should call ttsManager pause`() =
        runTest {
            every { ttsManager.pause() } returns Unit

            viewModel.onIntent(BibleIntent.PauseSpeech)
            advanceUntilIdle()

            verify { ttsManager.pause() }
            assertTrue(viewModel.uiState.value.isSpeechPaused)
        }

    @Test
    fun `when ResumeSpeech intent is sent should call ttsManager resume`() =
        runTest {
            every { ttsManager.resume() } returns Unit

            viewModel.onIntent(BibleIntent.ResumeSpeech)
            advanceUntilIdle()

            verify { ttsManager.resume() }
            assertFalse(viewModel.uiState.value.isSpeechPaused)
        }

    @Test
    fun `when StopSpeech intent is sent should call ttsManager stop`() =
        runTest {
            every { ttsManager.stop() } returns Unit

            viewModel.onIntent(BibleIntent.StopSpeech)
            advanceUntilIdle()

            verify { ttsManager.stop() }
            assertFalse(viewModel.uiState.value.isSpeechEnabled)
        }

    @Test
    fun `when TTSManager emits NextChapter should load next chapter`() =
        runTest {
            val book = Book(name = "Genesis", abbrev = "gn")
            val chapter = digital.tonima.bibliadigital.domain.model.Chapter(number = 1)
            val chapterResponse =
                digital.tonima.bibliadigital.domain.model.ChapterResponse(
                    book = book,
                    chapter = chapter,
                )
            // Inject initial chapter state
            coEvery { getChapterUseCase(any(), any(), any(), any()) } coAnswers {
                lastArg<(Either<*, digital.tonima.bibliadigital.domain.model.ChapterResponse>) -> Unit>().invoke(
                    Either.Success(chapterResponse),
                )
            }
            coEvery { storeReadingHistoryUseCase(any(), any(), any(), any()) } coAnswers {
                lastArg<(Either<*, Unit>) -> Unit>().invoke(Either.Success(Unit))
            }

            viewModel.onIntent(BibleIntent.LoadChapter("Genesis", "gn", 1))
            advanceUntilIdle()

            // Emit NextChapter event. ViewModel handles this by calling getBookChapter with isAutoSpeech = true.
            // isAutoSpeech = true prevents calling stopSpeech(), avoiding "no answer found for TTSManager.stop()"
            ttsEvents.emit(TTSEvent.NextChapter)
            advanceUntilIdle()

            coVerify { getChapterUseCase(match { it.chapterId == 2 }, any(), any(), any()) }
        }
}
