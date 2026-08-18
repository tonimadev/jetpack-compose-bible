package digital.tonima.bibliadigital.domain.core.computation

import digital.tonima.bibliadigital.domain.core.function.Either
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@ExperimentalCoroutinesApi
class ComputationTest {
    interface TestCapability : Api {
        fun getValue(): String
    }

    @Test
    fun `should run computation in context`() =
        runTest {
            val computation = Computation<TestCapability, String> { it.getValue() }
            val capability =
                object : TestCapability {
                    override fun getValue() = "Hello"
                }

            val result = computation.runInContext(capability)
            assertEquals("Hello", result)
        }

    @Test
    fun `should map result`() =
        runTest {
            val computation = Computation<TestCapability, Int> { 10 }
            val mapped = computation.map { it * 2 }

            val result =
                mapped.runInContext(
                    object : TestCapability {
                        override fun getValue() = ""
                    },
                )
            assertEquals(20, result)
        }

    @Test
    fun `should flatMap result`() =
        runTest {
            val computation = Computation<TestCapability, Int> { 10 }
            val flatMapped =
                computation.flatMap { val1 ->
                    Computation<TestCapability, Int> { val1 + 5 }
                }

            val result =
                flatMapped.runInContext(
                    object : TestCapability {
                        override fun getValue() = ""
                    },
                )
            assertEquals(15, result)
        }

    @Test
    fun `should flatMapResult with short-circuit on failure`() =
        runTest {
            val failComputation = Computation<TestCapability, Either<String, Int>> { Either.Fail("Error") }
            var wasCalled = false
            val pipeline =
                failComputation.flatMapResult { value: Int ->
                    wasCalled = true
                    Computation<TestCapability, Either<String, Int>> { Either.Success(value + 1) }
                }

            val result =
                pipeline.runInContext(
                    object : TestCapability {
                        override fun getValue() = ""
                    },
                )

            assertTrue(result is Either.Fail)
            assertEquals("Error", (result as Either.Fail).a)
            assertEquals(false, wasCalled)
        }

    @Test
    fun `should mapResult on success`() =
        runTest {
            val successComputation = Computation<TestCapability, Either<String, Int>> { Either.Success(10) }
            val mapped = successComputation.mapResult { it * 2 }

            val result =
                mapped.runInContext(
                    object : TestCapability {
                        override fun getValue() = ""
                    },
                )

            assertTrue(result is Either.Success)
            assertEquals(20, (result as Either.Success).b)
        }
}
