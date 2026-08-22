package digital.tonima.bibliadigital.feature.bible.impl

import digital.tonima.bibliadigital.core.common.core.computation.CapabilityRegistry
import digital.tonima.bibliadigital.core.common.core.computation.Computation
import digital.tonima.bibliadigital.core.common.core.exception.Failure.Error
import digital.tonima.bibliadigital.core.common.core.function.Either.Fail
import digital.tonima.bibliadigital.core.common.core.function.Either.Success
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

@ExperimentalCoroutinesApi
class StoreFontSizeUseCaseTest {
    private val mockDomainEffects: BibleDomainEffects = mockk()
    private val useCase = StoreFontSizeUseCase(mockDomainEffects)
    private val mockRegistry: CapabilityRegistry = mockk()

    @Test
    fun `when execute should store font size`() =
        runTest {
            val params = StoreFontSizeUseCase.Params(20)
            every { mockDomainEffects.storeFontSize(params.fontSize) } returns Computation { Success(Unit) }

            val result = useCase.execute(params).runInContext(mockRegistry)

            assertTrue(result is Success)
        }

    @Test
    fun `when domain effects return failure should return failure`() =
        runTest {
            val params = StoreFontSizeUseCase.Params(20)
            every { mockDomainEffects.storeFontSize(params.fontSize) } returns Computation { Fail(Error) }

            val result = useCase.execute(params).runInContext(mockRegistry)

            assertTrue(result is Fail)
        }
}
