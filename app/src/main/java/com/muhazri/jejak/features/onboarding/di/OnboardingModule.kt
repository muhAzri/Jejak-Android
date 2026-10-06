package com.muhazri.jejak.features.onboarding.di

import com.muhazri.jejak.features.onboarding.data.repositories.OnboardingRepositoryImpl
import com.muhazri.jejak.features.onboarding.data.services.LocationPermissionServiceImpl
import com.muhazri.jejak.features.onboarding.domain.repositories.LocationPermissionService
import com.muhazri.jejak.features.onboarding.domain.repositories.OnboardingRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface OnboardingModule {

    @Binds
    @Singleton
    fun bindOnboardingRepository(impl: OnboardingRepositoryImpl): OnboardingRepository

    @Binds
    @Singleton
    fun bindLocationPermissionService(impl: LocationPermissionServiceImpl): LocationPermissionService
}
