package digital.tonima.bibliadigital.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import digital.tonima.bibliadigital.data.datastore.PreferencesDataStore
import digital.tonima.bibliadigital.domain.repository.BibleRepository
import digital.tonima.bibliadigital.domain.usecases.DisableShowPressAndHoldVerseTutorialUseCase
import digital.tonima.bibliadigital.domain.usecases.GetBooksUseCase
import digital.tonima.bibliadigital.domain.usecases.GetChapterUseCase
import digital.tonima.bibliadigital.domain.usecases.GetFontSizeUseCase
import digital.tonima.bibliadigital.domain.usecases.GetShowPressAndHoldVerseTutorialUseCase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class UseCasesModule {
    @Provides
    @Singleton
    fun createGetBooksUseCase(repository: BibleRepository): GetBooksUseCase = GetBooksUseCase(repository)

    @Provides
    @Singleton
    fun createGetChaptersUseCase(repository: BibleRepository): GetChapterUseCase = GetChapterUseCase(repository)

    @Provides
    @Singleton
    fun createGetFontSizeUseCase(preferencesDataStore: PreferencesDataStore): GetFontSizeUseCase =
        GetFontSizeUseCase(preferencesDataStore)

    @Provides
    @Singleton
    fun createDisableShowPressAndHoldVerseTutorialUseCase(
        preferencesDataStore: PreferencesDataStore,
    ): DisableShowPressAndHoldVerseTutorialUseCase = DisableShowPressAndHoldVerseTutorialUseCase(preferencesDataStore)

    @Provides
    @Singleton
    fun createGetShowPressAndHoldVerseTutorialUseCase(
        preferencesDataStore: PreferencesDataStore,
    ): GetShowPressAndHoldVerseTutorialUseCase = GetShowPressAndHoldVerseTutorialUseCase(preferencesDataStore)
}
