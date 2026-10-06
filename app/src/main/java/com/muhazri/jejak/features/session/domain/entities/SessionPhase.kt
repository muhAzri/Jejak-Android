package com.muhazri.jejak.features.session.domain.entities

enum class SessionPhase {
    /** Waiting for the first good fix; nothing is recorded yet. */
    Searching,
    Recording,
    Paused,

    /** Stopped; the summary decides between saving and discarding. */
    Finished,
}
