package digital.tonima.bibliadigital.domain.usecases

import digital.tonima.bibliadigital.domain.core.exception.Failure
import digital.tonima.bibliadigital.domain.core.function.Either
import digital.tonima.bibliadigital.domain.model.Abbrev
import digital.tonima.bibliadigital.domain.model.BookResponse
import digital.tonima.bibliadigital.domain.repository.BibleRepository
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class GetBooksUseCaseTest {
    // Cenários de sucesso
    @Test
    fun `when run with params None should return a list of BookResponse`() =
        runBlocking {
            val mockRepository = mock(BibleRepository::class.java)
            val useCase = GetBooksUseCase(mockRepository)

            val mockBookList =
                listOf(
                    BookResponse(1, Abbrev(1, "gn")),
                    BookResponse(2, Abbrev(1, "ex")),
                )
            `when`(mockRepository.getBooks()).thenReturn(Either.Success(mockBookList))

            val result = useCase.run(UseCase.None())

            verify(mockRepository).getBooks()
            assertTrue(result is Either.Success)
            assertEquals(mockBookList, (result as Either.Success).b)
        }

    // Cenários de falha
    @Test
    fun `when repository returns failure should return failure`() =
        runBlocking {
            val mockRepository = mock(BibleRepository::class.java)
            val useCase = GetBooksUseCase(mockRepository)

            `when`(mockRepository.getBooks()).thenReturn(Either.Fail(Failure.Error))

            val result = useCase.run(UseCase.None())

            verify(mockRepository).getBooks()
            assertTrue(result is Either.Fail)
        }

    @Test
    fun `when repository throws an exception should return failure`() =
        runBlocking {
            val mockRepository = mock(BibleRepository::class.java)
            val useCase = GetBooksUseCase(mockRepository)

            `when`(mockRepository.getBooks()).thenThrow(
                RuntimeException("Getting books error"),
            )

            val result = useCase.run(UseCase.None())
            verify(mockRepository).getBooks()
            assertTrue(result is Either.Fail)
        }
}
