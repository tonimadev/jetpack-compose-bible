package digital.tonima.bibliadigital.domain.usecases

import digital.tonima.bibliadigital.data.datastore.PreferencesDataStore
import digital.tonima.bibliadigital.domain.core.exception.Failure
import digital.tonima.bibliadigital.domain.core.function.Either
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class DisableShowPressAndHoldVerseTutorialUseCaseTest {
    @Test
    fun `when run with params None should return success`() =
        runBlocking {
            val mockDataStore = mock(PreferencesDataStore::class.java)
            val useCase = DisableShowPressAndHoldVerseTutorialUseCase(mockDataStore)

            `when`(mockDataStore.disableShowPressAndHoldVerseTutorial()).thenReturn(Either.Success(Unit))

            val result = useCase.run(UseCase.None())

            verify(mockDataStore).disableShowPressAndHoldVerseTutorial()
            assertTrue(result is Either.Success)
        }

    @Test
    fun `when run with params None should call the preferences data store method once`(): Unit =
        runBlocking {
            val mockDataStore = mock(PreferencesDataStore::class.java)
            val useCase = DisableShowPressAndHoldVerseTutorialUseCase(mockDataStore)

            `when`(mockDataStore.disableShowPressAndHoldVerseTutorial()).thenReturn(
                Either.Success(
                    Unit,
                ),
            )

            useCase.run(UseCase.None())

            verify(mockDataStore, times(1)).disableShowPressAndHoldVerseTutorial()
        }

    @Test
    fun `when data store returns failure should return failure`() =
        runBlocking {
            val mockDataStore = mock(PreferencesDataStore::class.java)
            val useCase = DisableShowPressAndHoldVerseTutorialUseCase(mockDataStore)

            `when`(mockDataStore.disableShowPressAndHoldVerseTutorial()).thenReturn(
                Either.Fail(Failure.Error),
            )

            val result = useCase.run(UseCase.None())

            verify(mockDataStore, times(1)).disableShowPressAndHoldVerseTutorial()
            assertTrue(result is Either.Fail)
        }

    @Test
    fun `when data store throws an exception should return failure`() =
        runBlocking {
            val mockDataStore = mock(PreferencesDataStore::class.java)
            val useCase = DisableShowPressAndHoldVerseTutorialUseCase(mockDataStore)

            `when`(mockDataStore.disableShowPressAndHoldVerseTutorial()).thenThrow(RuntimeException("Data store error"))

            val result = useCase.run(UseCase.None())

            verify(mockDataStore, times(1)).disableShowPressAndHoldVerseTutorial()
            assertTrue(result is Either.Fail)
        }
}
