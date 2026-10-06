package com.muhazri.jejak.features.settings.data.repositories

import android.content.SharedPreferences
import androidx.core.content.edit
import com.muhazri.jejak.features.settings.domain.entities.DistanceUnit
import com.muhazri.jejak.features.settings.domain.repositories.SettingsRepository
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val preferences: SharedPreferences,
) : SettingsRepository {

    /** Defaults to the region's measurement system until the user picks one. */
    override fun distanceUnit(): DistanceUnit {
        val stored = preferences.getString(UNIT_KEY, null)
        return DistanceUnit.entries.firstOrNull { it.symbol == stored }
            ?: if (usesImperialDistances(Locale.getDefault().country)) {
                DistanceUnit.Miles
            } else {
                DistanceUnit.Kilometers
            }
    }

    override fun setDistanceUnit(unit: DistanceUnit) {
        preferences.edit { putString(UNIT_KEY, unit.symbol) }
    }

    private companion object {
        const val UNIT_KEY = "settings.distanceUnit"

        /**
         * The countries that measure road distance in miles. The UK is left out on purpose: it mixes
         * the two and the platform reports metric for it, as iOS does.
         */
        val IMPERIAL_COUNTRIES = setOf("US", "LR", "MM", "PR", "GU", "VI", "AS", "MP")

        fun usesImperialDistances(country: String) = country.uppercase(Locale.ROOT) in IMPERIAL_COUNTRIES
    }
}
