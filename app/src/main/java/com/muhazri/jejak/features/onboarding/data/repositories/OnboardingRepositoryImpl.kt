package com.muhazri.jejak.features.onboarding.data.repositories

import android.content.SharedPreferences
import androidx.core.content.edit
import com.muhazri.jejak.features.onboarding.domain.repositories.OnboardingRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OnboardingRepositoryImpl @Inject constructor(
    private val preferences: SharedPreferences,
) : OnboardingRepository {

    override fun isCompleted(): Boolean = preferences.getBoolean(KEY, false)

    override fun markCompleted() {
        preferences.edit { putBoolean(KEY, true) }
    }

    private companion object {
        const val KEY = "onboarding.completed"
    }
}
