package digital.tonima.bibliadigital.data.remote.bible

import digital.tonima.bibliadigital.domain.core.function.Either
import digital.tonima.bibliadigital.domain.model.Abbrev
import digital.tonima.bibliadigital.domain.model.BaseResponse
import digital.tonima.bibliadigital.domain.model.BookResponse
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@ExperimentalCoroutinesApi
class BibleEffectsTest {
    private val api = mockk<ChurchRoomApi>()
    private val bibleEffects = BibleEffects()

    @Test
    fun `getBooks should return DTOs`() =
        runTest {
            val booksDto = listOf(BookResponse(name = "Genesis", abbrev = Abbrev(pt = "gn")))
            val response = BaseResponse(data = booksDto)

            coEvery { api.getBooks() } returns response

            val computation = bibleEffects.getBooks()
            val result = computation.runInContext(api)

            assertTrue(result is Either.Success)
            val dtos = (result as Either.Success).b
            assertEquals(1, dtos.size)
            assertEquals("Genesis", dtos[0].name)
        }

    @Test
    fun `getBooks should propagate network errors`() =
        runTest {
            coEvery { api.getBooks() } throws Exception("Network timeout")

            val computation = bibleEffects.getBooks()
            val result = computation.runInContext(api)

            assertTrue(result is Either.Fail)
        }
}
