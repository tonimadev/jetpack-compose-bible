package digital.tonima.bibliadigital.domain.usecases

import digital.tonima.bibliadigital.domain.BibleDomainEffects
import digital.tonima.bibliadigital.domain.core.computation.CapabilityRegistry
import digital.tonima.bibliadigital.domain.core.computation.Computation
import digital.tonima.bibliadigital.domain.core.exception.Failure
import digital.tonima.bibliadigital.domain.core.function.Either
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@ExperimentalCoroutinesApi
class GetFontSizeUseCaseTest {
    private val mockDomainEffects: BibleDomainEffects = mockk()
    private val useCase = GetFontSizeUseCase(mockDomainEffects)
    private val mockRegistry: CapabilityRegistry = mockk()

    @Test
    fun `when execute should return font size`() =
        runTest {
            every { mockDomainEffects.getFontSize() } returns Computation { Either.Success(16) }

            val result = useCase.execute(UseCase.None()).runInContext(mockRegistry)

            assertTrue(result is Either.Success)
            assertEquals(16, (result as Either.Success).b)
        }

    @Test
    fun `when domain effects return failure should return failure`() =
        runTest {
            every { mockDomainEffects.getFontSize() } returns Computation { Either.Fail(Failure.Error) }

            val result = useCase.execute(UseCase.None()).runInContext(mockRegistry)

            assertTrue(result is Either.Fail)
        }
}
