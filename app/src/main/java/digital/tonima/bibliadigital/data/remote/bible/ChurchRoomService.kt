package digital.tonima.bibliadigital.data.remote.bible

import digital.tonima.bibliadigital.domain.model.BookResponse
import digital.tonima.bibliadigital.domain.model.ChapterResponse
import retrofit2.Call
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChurchRoomService
    @Inject
    constructor(retrofit: Retrofit) : ChurchRoomApi {
        private val api by lazy { retrofit.create(ChurchRoomApi::class.java) }

        override fun getBooks(): Call<List<BookResponse>> = api.getBooks()

        override fun getChapter(url: String): Call<ChapterResponse> = api.getChapter(url)
    }
