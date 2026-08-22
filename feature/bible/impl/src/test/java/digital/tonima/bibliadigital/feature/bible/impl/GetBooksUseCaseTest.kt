package digital.tonima.bibliadigital.feature.bible.impl

import digital.tonima.bibliadigital.core.common.core.computation.CapabilityRegistry
import digital.tonima.bibliadigital.core.common.core.computation.Computation
import digital.tonima.bibliadigital.core.common.core.function.Either
import digital.tonima.bibliadigital.core.common.core.network.NetworkError
import digital.tonima.bibliadigital.core.common.model.Book
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@ExperimentalCoroutinesApi
class GetBooksUseCaseTest {
    private val mockDomainEffects: BibleDomainEffects = mockk()
    private val useCase = GetBooksUseCase(mockDomainEffects)
    private val mockRegistry: CapabilityRegistry = mockk()

    @Test
    fun `when execute with params None should return a list of Book`() =
        runTest {
            val mockBookList = listOf(Book(id = 1, name = "Genesis"), Book(id = 2, name = "Exodus"))
            every { mockDomainEffects.getBooks() } returns Computation { Either.Success(mockBookList) }

            val result = useCase.execute(UseCase.None()).runInContext(mockRegistry)

            assertTrue(result is Either.Success)
            assertEquals(mockBookList, (result as Either.Success).b)
        }

    @Test
    fun `when domain effects return failure should return failure`() =
        runTest {
            every { mockDomainEffects.getBooks() } returns Computation { Either.Fail(NetworkError.ServerError) }

            val result = useCase.execute(UseCase.None()).runInContext(mockRegistry)

            assertTrue(result is Either.Fail)
        }
}
