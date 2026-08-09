package digital.tonima.bibliadigital.domain.usecases

import digital.tonima.bibliadigital.data.datastore.PreferencesDataStore
import digital.tonima.bibliadigital.domain.core.exception.Failure
import digital.tonima.bibliadigital.domain.core.function.Either
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test

class StoreFontSizeUseCaseTest {
    private val mockDataStore: PreferencesDataStore = mockk()
    private val useCase = StoreFontSizeUseCase(mockDataStore)

    @Test
    fun `when run should store font size`() =
        runBlocking {
            val params = StoreFontSizeUseCase.Params(20)
            coEvery { mockDataStore.storeFontSize(params.fontSize) } returns Either.Success(Unit)

            val result = useCase.run(params)

            coVerify { mockDataStore.storeFontSize(params.fontSize) }
            assertTrue(result is Either.Success)
        }

    @Test
    fun `when data store returns failure should return failure`() =
        runBlocking {
            val params = StoreFontSizeUseCase.Params(20)
            coEvery { mockDataStore.storeFontSize(params.fontSize) } returns Either.Fail(Failure.Error)

            val result = useCase.run(params)

            coVerify { mockDataStore.storeFontSize(params.fontSize) }
            assertTrue(result is Either.Fail)
        }
}
