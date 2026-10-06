package com.muhazri.jejak.features.settings.domain.usecases

import com.muhazri.jejak.features.settings.domain.entities.DistanceUnit
import com.muhazri.jejak.features.settings.domain.repositories.SettingsRepository
import javax.inject.Inject

class GetDistanceUnit @Inject constructor(private val repository: SettingsRepository) {
    operator fun invoke(): DistanceUnit = repository.distanceUnit()
}

class SetDistanceUnit @Inject constructor(private val repository: SettingsRepository) {
    operator fun invoke(unit: DistanceUnit) = repository.setDistanceUnit(unit)
}
