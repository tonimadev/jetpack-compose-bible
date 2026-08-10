package digital.tonima.bibliadigital.data.remote.bible

import digital.tonima.bibliadigital.domain.model.BaseResponse
import digital.tonima.bibliadigital.domain.model.BookResponse
import digital.tonima.bibliadigital.domain.model.ChapterResponse
import digital.tonima.bibliadigital.domain.model.Version
import retrofit2.Call
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChurchRoomService
    @Inject
    constructor(retrofit: Retrofit) : ChurchRoomApi {
        private val api by lazy { retrofit.create(ChurchRoomApi::class.java) }

        override fun getVersions(): Call<BaseResponse<List<Version>>> = api.getVersions()

        override fun getBooks(): Call<BaseResponse<List<BookResponse>>> = api.getBooks()

        override fun getChapter(
            version: String,
            book: String,
            chapter: Int,
        ): Call<BaseResponse<ChapterResponse>> = api.getChapter(version, book, chapter)

        override fun getChapterByUrl(url: String): Call<BaseResponse<ChapterResponse>> = api.getChapterByUrl(url)

        override fun getRandomVerse(version: String): Call<BaseResponse<Any>> = api.getRandomVerse(version)

        override fun search(
            version: String,
            query: String,
        ): Call<BaseResponse<Any>> = api.search(version, query)
    }
