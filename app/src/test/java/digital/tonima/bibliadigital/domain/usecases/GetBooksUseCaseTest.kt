package digital.tonima.bibliadigital.domain.usecases

import digital.tonima.bibliadigital.domain.core.exception.Failure
import digital.tonima.bibliadigital.domain.core.function.Either
import digital.tonima.bibliadigital.domain.model.BookResponse
import digital.tonima.bibliadigital.domain.repository.BibleRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.runBlocking
import org.junit.Test

class GetBooksUseCaseTest {
    private val mockRepository: BibleRepository = mockk()
    private val useCase = GetBooksUseCase(mockRepository)

    @Test
    fun `when run with params None should return a list of BookResponse`() =
        runBlocking {
            val mockBookList =
                listOf(
                    BookResponse(1, "gn", name = "Genesis"),
                    BookResponse(2, "ex", name = "Exodus"),
                )
            coEvery { mockRepository.getBooks() } returns Either.Success(mockBookList)

            val result = useCase.run(UseCase.None())

            coVerify { mockRepository.getBooks() }
            assertTrue(result is Either.Success)
            assertEquals(mockBookList, (result as Either.Success).b)
        }

    @Test
    fun `when repository returns failure should return failure`() =
        runBlocking {
            coEvery { mockRepository.getBooks() } returns Either.Fail(Failure.Error)

            val result = useCase.run(UseCase.None())

            coVerify { mockRepository.getBooks() }
            assertTrue(result is Either.Fail)
        }
}
