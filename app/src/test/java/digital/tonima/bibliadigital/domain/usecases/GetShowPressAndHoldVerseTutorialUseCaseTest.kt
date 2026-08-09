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

class GetShowPressAndHoldVerseTutorialUseCaseTest {
    private val mockDataStore: PreferencesDataStore = mockk()
    private val useCase = GetShowPressAndHoldVerseTutorialUseCase(mockDataStore)

    @Test
    fun `when run should return tutorial visibility`() =
        runBlocking {
            coEvery { mockDataStore.readShowPressAndHoldVerseTutorial() } returns Either.Success(true)

            val result = useCase.run(UseCase.None())

            coVerify { mockDataStore.readShowPressAndHoldVerseTutorial() }
            assertTrue(result is Either.Success)
            assertEquals(true, (result as Either.Success).b)
        }

    @Test
    fun `when data store returns failure should return failure`() =
        runBlocking {
            coEvery { mockDataStore.readShowPressAndHoldVerseTutorial() } returns Either.Fail(Failure.Error)

            val result = useCase.run(UseCase.None())

            coVerify { mockDataStore.readShowPressAndHoldVerseTutorial() }
            assertTrue(result is Either.Fail)
        }
}
