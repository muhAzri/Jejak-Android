package com.muhazri.jejak.features.settings.di

import com.muhazri.jejak.features.settings.data.repositories.SettingsRepositoryImpl
import com.muhazri.jejak.features.settings.domain.repositories.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface SettingsModule {

    @Binds
    @Singleton
    fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository
}
