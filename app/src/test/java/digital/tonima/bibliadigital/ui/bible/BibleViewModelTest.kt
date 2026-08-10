package digital.tonima.bibliadigital.ui.bible

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import digital.tonima.bibliadigital.domain.core.function.Either
import digital.tonima.bibliadigital.domain.model.BookResponse
import digital.tonima.bibliadigital.domain.usecases.DisableShowPressAndHoldVerseTutorialUseCase
import digital.tonima.bibliadigital.domain.usecases.GetBooksUseCase
import digital.tonima.bibliadigital.domain.usecases.GetChapterUseCase
import digital.tonima.bibliadigital.domain.usecases.GetFontSizeUseCase
import digital.tonima.bibliadigital.domain.usecases.GetShowPressAndHoldVerseTutorialUseCase
import digital.tonima.bibliadigital.domain.usecases.StoreFontSizeUseCase
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

        viewModel =
            BibleViewModel(
                getBooksUseCase,
                getChapterUseCase,
                getFontSizeUseCase,
                storeFontSizeUseCase,
                disableShowPressAndHoldVerseTutorialUseCase,
                getStoreShowPressAndHoldVerseTutorial,
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

            assertEquals(16, viewModel.uiState.value.fontSize.value.toInt())
            assertEquals(true, viewModel.uiState.value.showTutorial)
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
}
