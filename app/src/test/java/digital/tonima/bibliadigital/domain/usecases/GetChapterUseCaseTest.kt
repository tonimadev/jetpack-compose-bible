package digital.tonima.bibliadigital.domain.usecases

import digital.tonima.bibliadigital.domain.core.exception.Failure
import digital.tonima.bibliadigital.domain.core.function.Either
import digital.tonima.bibliadigital.domain.model.Abbrev
import digital.tonima.bibliadigital.domain.model.Book
import digital.tonima.bibliadigital.domain.model.Chapter
import digital.tonima.bibliadigital.domain.model.ChapterResponse
import digital.tonima.bibliadigital.domain.repository.BibleRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetChapterUseCaseTest {
    private val mockRepository: BibleRepository = mockk()
    private val useCase = GetChapterUseCase(mockRepository)

    @Test
    fun `when run should return a ChapterResponse`() =
        runBlocking {
            val params = GetChapterUseCase.Params("Genesis", "gn", 1)
            val mockChapterResponse =
                ChapterResponse(
                    book = Book(id = 1, abbrev = Abbrev(pt = "gn"), name = "Genesis"),
                    chapter = Chapter(number = 1, verses = 31),
                    verses = emptyList(),
                )

            coEvery { mockRepository.getChapter(params.bookName, params.bookAbbrev, params.chapterId) }
                .returns(Either.Success(mockChapterResponse))

            val result = useCase.run(params)

            coVerify { mockRepository.getChapter(params.bookName, params.bookAbbrev, params.chapterId) }
            assertTrue(result is Either.Success)
            assertEquals(mockChapterResponse, (result as Either.Success).b)
        }

    @Test
    fun `when repository returns failure should return failure`() =
        runBlocking {
            val params = GetChapterUseCase.Params("Genesis", "gn", 1)

            coEvery { mockRepository.getChapter(params.bookName, params.bookAbbrev, params.chapterId) }
                .returns(Either.Fail(Failure.Error))

            val result = useCase.run(params)

            coVerify { mockRepository.getChapter(params.bookName, params.bookAbbrev, params.chapterId) }
            assertTrue(result is Either.Fail)
        }
}
