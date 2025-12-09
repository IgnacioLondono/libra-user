package com.empresa.libra_users.di

import android.content.Context
import com.empresa.libra_users.data.UserPreferencesRepository
import com.empresa.libra_users.data.local.TokenManager
import com.empresa.libra_users.data.repository.BookRepository
import com.empresa.libra_users.data.repository.LoanRepository
import com.empresa.libra_users.data.repository.NotificationRepository
import com.empresa.libra_users.data.repository.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideUserPreferencesRepository(
        @ApplicationContext context: Context
    ): UserPreferencesRepository {
        return UserPreferencesRepository(context)
    }

    @Provides
    @Singleton
    fun provideTokenManager(
        userPreferencesRepository: UserPreferencesRepository
    ): TokenManager {
        return TokenManager(userPreferencesRepository)
    }

    // Los repositorios ahora se inyectan automáticamente con @Inject
    // No necesitan dependencias de Room/DAO
}
