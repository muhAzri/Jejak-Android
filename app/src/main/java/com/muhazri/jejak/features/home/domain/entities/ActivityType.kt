package com.muhazri.jejak.features.home.domain.entities

import kotlinx.serialization.Serializable

/** Annotated so the saved-session format is pinned to these names, not resolved reflectively. */
@Serializable
enum class ActivityType { Run, Walk }
