package com.muhazri.jejak.features.settings.domain.repositories

import com.muhazri.jejak.features.settings.domain.entities.DistanceUnit

interface SettingsRepository {
    fun distanceUnit(): DistanceUnit
    fun setDistanceUnit(unit: DistanceUnit)
}
