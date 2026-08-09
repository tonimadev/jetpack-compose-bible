package digital.tonima.bibliadigital.data.remote.bible

import digital.tonima.bibliadigital.domain.model.BookResponse
import digital.tonima.bibliadigital.domain.model.ChapterResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Url

interface ChurchRoomApi {
    @GET("books")
    fun getBooks(): Call<List<BookResponse>>

    @GET
    fun getChapter(
        @Url url: String,
    ): Call<ChapterResponse>
}
