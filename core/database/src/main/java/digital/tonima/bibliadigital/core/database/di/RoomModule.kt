package digital.tonima.bibliadigital.core.database.di

import android.app.Application
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import digital.tonima.bibliadigital.core.common.model.ChurchDao
import digital.tonima.bibliadigital.core.common.model.ChurchDatabase
import digital.tonima.bibliadigital.core.common.model.DatabaseConstants.BIBLE_DB_NAME
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
