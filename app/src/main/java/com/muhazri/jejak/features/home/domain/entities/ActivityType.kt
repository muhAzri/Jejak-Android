package com.muhazri.jejak.features.home.domain.entities

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

/**
 * @Serializable pins the saved-session format to these names rather than resolving them
 * reflectively; @Keep stops R8 renaming them, which would break the type-safe navigation route
 * that carries an activity.
 */
@Keep
@Serializable
enum class ActivityType { Run, Walk }
