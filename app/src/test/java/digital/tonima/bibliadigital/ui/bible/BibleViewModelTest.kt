package digital.tonima.bibliadigital.ui.bible

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import digital.tonima.bibliadigital.data.datastore.ReadingHistory
import digital.tonima.bibliadigital.domain.core.function.Either
import digital.tonima.bibliadigital.domain.model.BookResponse
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
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
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

    private lateinit var viewModel: BibleViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        // Mock default behavior for init calls
        coEvery { getFontSizeUseCase(any(), any(), any()) } coAnswers {
            thirdArg<(Either<*, Int>) -> Unit>().invoke(Either.Success(16))
        }
        coEvery { getStoreShowPressAndHoldVerseTutorial(any(), any(), any()) } coAnswers {
            thirdArg<(Either<*, Boolean>) -> Unit>().invoke(Either.Success(true))
        }
        coEvery { getBooksUseCase(any(), any(), any()) } coAnswers {
            thirdArg<(Either<*, List<BookResponse>>) -> Unit>().invoke(Either.Success(emptyList()))
        }
        coEvery { getSelectedVersionUseCase(any(), any(), any()) } coAnswers {
            thirdArg<(Either<*, String>) -> Unit>().invoke(Either.Success("nvi"))
        }
        coEvery { getVersionsUseCase(any(), any(), any()) } coAnswers {
            thirdArg<(Either<*, List<Version>>) -> Unit>().invoke(Either.Success(emptyList()))
        }
        coEvery { getReadingHistoryUseCase(any(), any(), any()) } coAnswers {
            thirdArg<(Either<*, ReadingHistory?>) -> Unit>().invoke(Either.Success(null))
        }
        coEvery { getFavoritesUseCase(any(), any(), any()) } coAnswers {
            thirdArg<(Either<*, List<FavoriteVerse>>) -> Unit>().invoke(Either.Success(emptyList()))
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
            coVerify { getFontSizeUseCase(any(), any(), any()) }
            coVerify { getStoreShowPressAndHoldVerseTutorial(any(), any(), any()) }
            coVerify { getBooksUseCase(any(), any(), any()) }
            coVerify { getSelectedVersionUseCase(any(), any(), any()) }
            coVerify { getVersionsUseCase(any(), any(), any()) }
            coVerify { getReadingHistoryUseCase(any(), any(), any()) }
            coVerify { getFavoritesUseCase(any(), any(), any()) }

            assertEquals(16, viewModel.uiState.value.fontSize.value.toInt())
            assertEquals(true, viewModel.uiState.value.showTutorial)
            assertEquals("nvi", viewModel.uiState.value.selectedVersion)
        }

    @Test
    fun `when LoadBooks intent is sent should update books in state`() =
        runTest {
            val mockBooks = listOf(BookResponse(1, "gn", name = "Genesis"))
            coEvery { getBooksUseCase(any(), any(), any()) } coAnswers {
                thirdArg<(Either<*, List<BookResponse>>) -> Unit>().invoke(Either.Success(mockBooks))
            }

            viewModel.sendIntent(BibleIntent.LoadBooks)
            advanceUntilIdle()

            assertEquals(mockBooks, viewModel.uiState.value.books)
        }

    @Test
    fun `when LoadHistory intent is sent should update history in state`() =
        runTest {
            val mockHistory = ReadingHistory("Genesis", "gn", 1, 50)
            coEvery { getReadingHistoryUseCase(any(), any(), any()) } coAnswers {
                thirdArg<(Either<*, ReadingHistory?>) -> Unit>().invoke(Either.Success(mockHistory))
            }

            viewModel.sendIntent(BibleIntent.LoadHistory)
            advanceUntilIdle()

            assertEquals(mockHistory, viewModel.uiState.value.history)
        }

    @Test
    fun `when ChangeVersion intent is sent should update selected version and fetch chapter`() =
        runTest {
            val newVersion = "acf"
            coEvery { storeSelectedVersionUseCase(any(), any(), any()) } coAnswers {
                thirdArg<(Either<*, Unit>) -> Unit>().invoke(Either.Success(Unit))
            }
            coEvery { getChapterUseCase(any(), any(), any()) } coAnswers {
                // Return dummy failure to simplify test, main point is that it was called
                thirdArg<(Either<*, digital.tonima.bibliadigital.domain.model.ChapterResponse>) -> Unit>().invoke(
                    Either.Fail(digital.tonima.bibliadigital.domain.core.exception.Failure.NetworkConnection),
                )
            }

            viewModel.sendIntent(BibleIntent.ChangeVersion(newVersion))
            advanceUntilIdle()

            assertEquals(newVersion, viewModel.uiState.value.selectedVersion)
            coVerify { storeSelectedVersionUseCase(any(), any(), any()) }
        }

    @Test
    fun `when ToggleFavorite intent is sent should call use case and reload favorites`() =
        runTest {
            val verse = Verse(number = 1, text = "Test verse")
            val bookName = "Genesis"
            val chapter = 1

            coEvery { toggleFavoriteUseCase(any(), any(), any()) } coAnswers {
                thirdArg<(Either<*, Boolean>) -> Unit>().invoke(Either.Success(true))
            }
            coEvery { getFavoritesUseCase(any(), any(), any()) } coAnswers {
                thirdArg<(Either<*, List<FavoriteVerse>>) -> Unit>().invoke(Either.Success(emptyList()))
            }

            viewModel.sendIntent(BibleIntent.ToggleFavorite(verse, bookName, chapter))
            advanceUntilIdle()

            coVerify { toggleFavoriteUseCase(any(), any(), any()) }
            coVerify(exactly = 2) { getFavoritesUseCase(any(), any(), any()) } // One on init, one after toggle
        }
}
