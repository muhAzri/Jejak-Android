package com.muhazri.jejak.features.session.di

import com.muhazri.jejak.features.session.data.services.LocationTrackingServiceImpl
import com.muhazri.jejak.features.session.domain.repositories.LocationTrackingService
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface SessionModule {

    @Binds
    @Singleton
    fun bindLocationTrackingService(impl: LocationTrackingServiceImpl): LocationTrackingService
}
