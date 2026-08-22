package digital.tonima.bibliadigital.feature.bible.impl

import digital.tonima.bibliadigital.core.common.core.computation.CapabilityRegistry
import digital.tonima.bibliadigital.core.common.core.computation.Computation
import digital.tonima.bibliadigital.core.common.core.exception.Failure
import digital.tonima.bibliadigital.core.common.core.function.Either
import digital.tonima.bibliadigital.core.common.model.Book
import digital.tonima.bibliadigital.core.common.model.Chapter
import digital.tonima.bibliadigital.core.common.model.ChapterResponse
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@ExperimentalCoroutinesApi
class GetChapterUseCaseTest {
    private val mockDomainEffects: BibleDomainEffects = mockk()
    private val useCase = GetChapterUseCase(mockDomainEffects)
    private val mockRegistry: CapabilityRegistry = mockk()

    @Test
    fun `when execute should return a ChapterResponse`() =
        runTest {
            val params = GetChapterUseCase.Params("Genesis", "gn", 1, "nvi")
            val mockChapterResponse =
                ChapterResponse(
                    book = Book(id = 1, abbrev = "gn", name = "Genesis"),
                    chapter = Chapter(number = 1, verses = 31),
                    verses = emptyList(),
                )

            every { mockDomainEffects.getChapter(any(), any(), any(), any()) } returns
                Computation {
                    Either.Success(mockChapterResponse)
                }

            val result = useCase.execute(params).runInContext(mockRegistry)

            assertTrue(result is Either.Success)
            assertEquals(mockChapterResponse, (result as Either.Success).b)
        }

    @Test
    fun `when domain effects return failure should return failure`() =
        runTest {
            val params = GetChapterUseCase.Params("Genesis", "gn", 1, "nvi")

            every { mockDomainEffects.getChapter(any(), any(), any(), any()) } returns
                Computation {
                    Either.Fail(Failure.Error)
                }

            val result = useCase.execute(params).runInContext(mockRegistry)

            assertTrue(result is Either.Fail)
        }
}
