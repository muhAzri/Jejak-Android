package com.muhazri.jejak.features.home.di

import android.content.Context
import com.muhazri.jejak.core.di.IoDispatcher
import com.muhazri.jejak.features.home.data.repositories.SessionRepositoryImpl
import com.muhazri.jejak.features.home.domain.repositories.SessionRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.File
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.serialization.json.Json

@Module
@InstallIn(SingletonComponent::class)
object HomeModule {

    /**
     * Sessions are the app's only copy of the user's history, so they live in private app storage
     * rather than the cache the map tiles use.
     */
    @Provides
    @Singleton
    fun provideSessionRepository(
        @ApplicationContext context: Context,
        json: Json,
        @IoDispatcher io: CoroutineDispatcher,
    ): SessionRepository = SessionRepositoryImpl(
        file = File(context.filesDir, "sessions.json"),
        json = json,
        io = io,
    )
}
