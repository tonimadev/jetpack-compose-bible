package digital.tonima.bibliadigital.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import digital.tonima.bibliadigital.data.datastore.PreferencesDataStore
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DataStoreModule {
    @Provides
    @Singleton
    fun providesPreferencesDataStore(
        @ApplicationContext context: Context,
    ): PreferencesDataStore = PreferencesDataStore(context)
}
