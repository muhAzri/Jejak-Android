package com.muhazri.jejak.core.di

import com.muhazri.jejak.core.time.TimeSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TimeModule {

    @Provides
    @Singleton
    fun provideTimeSource(): TimeSource = TimeSource.System
}
