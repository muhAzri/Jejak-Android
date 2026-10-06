package com.muhazri.jejak.features.settings.domain.entities

enum class DistanceUnit(val symbol: String, val metersPerUnit: Double) {
    Kilometers("km", 1_000.0),
    Miles("mi", 1_609.344),
}
