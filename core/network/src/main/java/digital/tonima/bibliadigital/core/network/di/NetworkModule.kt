package digital.tonima.bibliadigital.core.network.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import digital.tonima.bibliadigital.core.common.constants.BIBLE_BASE_URL
import digital.tonima.bibliadigital.core.common.constants.SOCKET_TIMEOUT
import digital.tonima.bibliadigital.core.common.core.computation.CapabilityRegistry
import digital.tonima.bibliadigital.core.common.core.plataform.NetworkHandler
import digital.tonima.bibliadigital.core.common.model.ChurchDao
import digital.tonima.bibliadigital.core.database.datastore.PreferencesDataStore
import digital.tonima.bibliadigital.core.network.BuildConfig
import digital.tonima.bibliadigital.core.network.ChurchRoomApi
import digital.tonima.bibliadigital.core.network.HeaderInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {
    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit =
        Retrofit.Builder()
            .baseUrl(BIBLE_BASE_URL)
            .client(createClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun createBibleService(retrofit: Retrofit): ChurchRoomApi = retrofit.create(ChurchRoomApi::class.java)

    @Provides
    @Singleton
    fun provideCapabilityRegistry(
        api: ChurchRoomApi,
        dao: ChurchDao,
        dataStore: PreferencesDataStore,
        networkHandler: NetworkHandler,
    ): CapabilityRegistry =
        CapabilityRegistry.Builder()
            .register(ChurchRoomApi::class.java, api)
            .register(ChurchDao::class.java, dao)
            .register(PreferencesDataStore::class.java, dataStore)
            .register(NetworkHandler::class.java, networkHandler)
            .build()

    private fun createClient(): OkHttpClient {
        val okHttpClientBuilder: OkHttpClient.Builder = OkHttpClient.Builder()
        if (BuildConfig.DEBUG) {
            val loggingInterceptor =
                HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
            okHttpClientBuilder.addInterceptor(loggingInterceptor)
        }
        val headerInterceptor = HeaderInterceptor()
        okHttpClientBuilder.addInterceptor(headerInterceptor)
        okHttpClientBuilder.connectTimeout(SOCKET_TIMEOUT, TimeUnit.MILLISECONDS)
        okHttpClientBuilder.readTimeout(SOCKET_TIMEOUT, TimeUnit.MILLISECONDS)

        return okHttpClientBuilder.build()
    }
}
