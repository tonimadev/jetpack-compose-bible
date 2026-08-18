package digital.tonima.bibliadigital.data.remote.bible

import digital.tonima.bibliadigital.domain.core.computation.Get
import digital.tonima.bibliadigital.domain.model.BaseResponse
import digital.tonima.bibliadigital.domain.model.BookResponse
import digital.tonima.bibliadigital.domain.model.ChapterResponse
import digital.tonima.bibliadigital.domain.model.Version
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url

interface ChurchRoomApi : Get {
    @GET("versions")
    suspend fun getVersions(): BaseResponse<List<Version>>

    @GET("books")
    suspend fun getBooks(): BaseResponse<List<BookResponse>>

    @GET("versions/{version}/books/{book}/chapters/{chapter}")
    suspend fun getChapter(
        @Path("version") version: String,
        @Path("book") book: String,
        @Path("chapter") chapter: Int,
    ): BaseResponse<ChapterResponse>

    @GET
    suspend fun getChapterByUrl(
        @Url url: String,
    ): BaseResponse<ChapterResponse>

    @GET("versions/{version}/random")
    suspend fun getRandomVerse(
        @Path("version") version: String,
    ): BaseResponse<Any>

    @GET("versions/{version}/search")
    suspend fun search(
        @Path("version") version: String,
        @Query("q") query: String,
    ): BaseResponse<Any>
}
