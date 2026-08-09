package digital.tonima.bibliadigital.di

import android.app.Application
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import digital.tonima.bibliadigital.data.local.room.ChurchDao
import digital.tonima.bibliadigital.data.local.room.ChurchDatabase
import digital.tonima.bibliadigital.data.local.room.ChurchDatabase.Companion.BIBLE_DB_NAME
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class RoomModule {
    @Provides
    @Singleton
    fun providesBibleDatabase(application: Application): ChurchDatabase =
        Room
            .databaseBuilder(application, ChurchDatabase::class.java, BIBLE_DB_NAME)
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    @Singleton
    fun providesBibleDao(database: ChurchDatabase): ChurchDao = database.churchDao()
}
