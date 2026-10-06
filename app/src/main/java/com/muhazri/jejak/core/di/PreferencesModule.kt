package com.muhazri.jejak.core.di

import android.content.Context
import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** The small key-value store behind onboarding, the distance unit and the location grant. */
@Module
@InstallIn(SingletonComponent::class)
object PreferencesModule {

    @Provides
    @Singleton
    fun providePreferences(@ApplicationContext context: Context): SharedPreferences =
        context.getSharedPreferences("jejak.settings", Context.MODE_PRIVATE)
}
