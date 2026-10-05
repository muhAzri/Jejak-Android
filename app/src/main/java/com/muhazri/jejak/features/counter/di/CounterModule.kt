package com.muhazri.jejak.features.counter.di

import com.muhazri.jejak.features.counter.data.repositories.CounterRepositoryImpl
import com.muhazri.jejak.features.counter.domain.repositories.CounterRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CounterModule {

    @Binds
    @Singleton
    abstract fun bindCounterRepository(impl: CounterRepositoryImpl): CounterRepository
}
