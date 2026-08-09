package digital.tonima.bibliadigital.domain.usecases

import digital.tonima.bibliadigital.data.datastore.PreferencesDataStore
import digital.tonima.bibliadigital.domain.core.exception.Failure
import digital.tonima.bibliadigital.domain.core.function.Either
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetFontSizeUseCaseTest {
    private val mockDataStore: PreferencesDataStore = mockk()
    private val useCase = GetFontSizeUseCase(mockDataStore)

    @Test
    fun `when run should return font size`() =
        runBlocking {
            val fontSize = 16
            coEvery { mockDataStore.readFontSize() } returns Either.Success(fontSize)

            val result = useCase.run(UseCase.None())

            coVerify { mockDataStore.readFontSize() }
            assertTrue(result is Either.Success)
            assertEquals(fontSize, (result as Either.Success).b)
        }

    @Test
    fun `when data store returns failure should return failure`() =
        runBlocking {
            coEvery { mockDataStore.readFontSize() } returns Either.Fail(Failure.Error)

            val result = useCase.run(UseCase.None())

            coVerify { mockDataStore.readFontSize() }
            assertTrue(result is Either.Fail)
        }
}
