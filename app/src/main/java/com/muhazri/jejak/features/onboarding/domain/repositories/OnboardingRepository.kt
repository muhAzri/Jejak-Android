package com.muhazri.jejak.features.onboarding.domain.repositories

interface OnboardingRepository {
    fun isCompleted(): Boolean
    fun markCompleted()
}
