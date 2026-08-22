package digital.tonima.bibliadigital.feature.bible.impl

import digital.tonima.bibliadigital.core.common.core.computation.CapabilityRegistry
import digital.tonima.bibliadigital.core.common.core.computation.Computation
import digital.tonima.bibliadigital.core.common.core.exception.Failure
import digital.tonima.bibliadigital.core.common.core.function.Either
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

@ExperimentalCoroutinesApi
class DisableShowPressAndHoldVerseTutorialUseCaseTest {
    private val mockDomainEffects: BibleDomainEffects = mockk()
    private val useCase = DisableShowPressAndHoldVerseTutorialUseCase(mockDomainEffects)
    private val mockRegistry: CapabilityRegistry = mockk()

    @Test
    fun `when execute with params None should return success`() =
        runTest {
            every { mockDomainEffects.disableTutorial() } returns Computation { Either.Success(Unit) }

            val result = useCase.execute(UseCase.None()).runInContext(mockRegistry)

            assertTrue(result is Either.Success)
        }

    @Test
    fun `when domain effects return failure should return failure`() =
        runTest {
            every { mockDomainEffects.disableTutorial() } returns Computation { Either.Fail(Failure.Error) }

            val result = useCase.execute(UseCase.None()).runInContext(mockRegistry)

            assertTrue(result is Either.Fail)
        }
}
