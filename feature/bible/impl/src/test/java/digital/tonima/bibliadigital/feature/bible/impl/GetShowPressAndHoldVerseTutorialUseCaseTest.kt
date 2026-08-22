package digital.tonima.bibliadigital.feature.bible.impl

import digital.tonima.bibliadigital.core.common.core.computation.CapabilityRegistry
import digital.tonima.bibliadigital.core.common.core.computation.Computation
import digital.tonima.bibliadigital.core.common.core.exception.Failure
import digital.tonima.bibliadigital.core.common.core.function.Either
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@ExperimentalCoroutinesApi
class GetShowPressAndHoldVerseTutorialUseCaseTest {
    private val mockDomainEffects: BibleDomainEffects = mockk()
    private val useCase = GetShowPressAndHoldVerseTutorialUseCase(mockDomainEffects)
    private val mockRegistry: CapabilityRegistry = mockk()

    @Test
    fun `when execute should return tutorial visibility`() =
        runTest {
            every { mockDomainEffects.getTutorialStatus() } returns Computation { Either.Success(true) }

            val result = useCase.execute(UseCase.None()).runInContext(mockRegistry)

            assertTrue(result is Either.Success)
            assertEquals(true, (result as Either.Success).b)
        }

    @Test
    fun `when domain effects return failure should return failure`() =
        runTest {
            every { mockDomainEffects.getTutorialStatus() } returns Computation { Either.Fail(Failure.Error) }

            val result = useCase.execute(UseCase.None()).runInContext(mockRegistry)

            assertTrue(result is Either.Fail)
        }
}
