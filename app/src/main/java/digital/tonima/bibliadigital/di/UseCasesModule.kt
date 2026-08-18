package digital.tonima.bibliadigital.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import digital.tonima.bibliadigital.domain.BibleDomainEffects
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
    fun createGetBooksUseCase(bibleDomainEffects: BibleDomainEffects): GetBooksUseCase =
        GetBooksUseCase(bibleDomainEffects)

    @Provides
    @Singleton
    fun createGetChaptersUseCase(bibleDomainEffects: BibleDomainEffects): GetChapterUseCase =
        GetChapterUseCase(bibleDomainEffects)

    @Provides
    @Singleton
    fun createGetFontSizeUseCase(bibleDomainEffects: BibleDomainEffects): GetFontSizeUseCase =
        GetFontSizeUseCase(bibleDomainEffects)

    @Provides
    @Singleton
    fun createDisableShowPressAndHoldVerseTutorialUseCase(
        bibleDomainEffects: BibleDomainEffects,
    ): DisableShowPressAndHoldVerseTutorialUseCase = DisableShowPressAndHoldVerseTutorialUseCase(bibleDomainEffects)

    @Provides
    @Singleton
    fun createGetShowPressAndHoldVerseTutorialUseCase(
        bibleDomainEffects: BibleDomainEffects,
    ): GetShowPressAndHoldVerseTutorialUseCase = GetShowPressAndHoldVerseTutorialUseCase(bibleDomainEffects)
}
