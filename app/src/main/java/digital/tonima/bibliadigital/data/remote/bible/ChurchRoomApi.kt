package digital.tonima.bibliadigital.data.remote.bible

import digital.tonima.bibliadigital.domain.model.BaseResponse
import digital.tonima.bibliadigital.domain.model.BookResponse
import digital.tonima.bibliadigital.domain.model.ChapterResponse
import digital.tonima.bibliadigital.domain.model.Version
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url

interface ChurchRoomApi {
    @GET("versions")
    fun getVersions(): Call<BaseResponse<List<Version>>>

    @GET("books")
    fun getBooks(): Call<BaseResponse<List<BookResponse>>>

    @GET("versions/{version}/books/{book}/chapters/{chapter}")
    fun getChapter(
        @Path("version") version: String,
        @Path("book") book: String,
        @Path("chapter") chapter: Int,
    ): Call<BaseResponse<ChapterResponse>>

    @GET
    fun getChapterByUrl(
        @Url url: String,
    ): Call<BaseResponse<ChapterResponse>>

    @GET("versions/{version}/random")
    fun getRandomVerse(
        @Path("version") version: String,
    ): Call<BaseResponse<Any>> // Using Any for now as we don't have a model yet

    @GET("versions/{version}/search")
    fun search(
        @Path("version") version: String,
        @Query("q") query: String,
    ): Call<BaseResponse<Any>>
}
